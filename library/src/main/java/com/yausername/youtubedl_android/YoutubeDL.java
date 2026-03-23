package com.yausername.youtubedl_android;

import android.content.Context;
import android.os.Build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yausername.youtubedl_android.mapper.VideoInfo;
import com.yausername.youtubedl_android.utils.SharedPrefsHelper;
import com.yausername.youtubedl_android.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeDL {

    // region Constants
    public static final String baseName = "youtubedl-android";
    public static final String ytdlpDirName = "yt-dlp";
    public static final String ytdlpBin = "yt-dlp";

    private static final String packagesRoot = "packages";
    private static final String pythonBinName = "libpython.so";
    private static final String pythonLibName = "libpython.zip.so";
    private static final String pythonDirName = "python";
    private static final String ffmpegDirName = "ffmpeg";
    private static final String ffmpegBinName = "libffmpeg.so";
    private static final String quickJsBinName = "libqjs.so";
    private static final String aria2cDirName = "aria2c";
    private static final String pythonLibVersion = "pythonLibVersion";
    // endregion

    // region Singleton
    private static YoutubeDL instance;

    public static YoutubeDL getInstance() {
        if (instance == null) {
            instance = new YoutubeDL();
        }
        return instance;
    }

    private YoutubeDL() {}
    // endregion

    // region Fields
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private boolean initialized = false;
    private File pythonPath = null;
    private File ffmpegPath = null;
    private File quickJsPath = null;
    private File ytdlpPath = null;
    private File binDir = null;
    private String ENV_LD_LIBRARY_PATH = null;
    private String ENV_SSL_CERT_FILE = null;
    private String ENV_PYTHONHOME = null;
    private String TMPDIR = "";
    private final Map<String, Process> idProcessMap =
            Collections.synchronizedMap(new HashMap<>());
    // endregion

    // region Initialization

    public synchronized void init(Context appContext) throws YoutubeDLException {
        if (initialized) return;

        File baseDir = new File(appContext.getNoBackupFilesDir(), baseName);
        if (!baseDir.exists()) baseDir.mkdir();

        File packagesDir = new File(baseDir, packagesRoot);
        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);
        pythonPath = new File(binDir, pythonBinName);
        ffmpegPath = new File(binDir, ffmpegBinName);
        quickJsPath = new File(binDir, quickJsBinName);

        File pythonDir = new File(packagesDir, pythonDirName);
        File ffmpegDir = new File(packagesDir, ffmpegDirName);
        File aria2cDir = new File(packagesDir, aria2cDirName);
        File ytdlpDir = new File(baseDir, ytdlpDirName);
        ytdlpPath = new File(ytdlpDir, ytdlpBin);

        ENV_LD_LIBRARY_PATH = pythonDir.getAbsolutePath() + "/usr/lib" + ":" +
                ffmpegDir.getAbsolutePath() + "/usr/lib" + ":" +
                aria2cDir.getAbsolutePath() + "/usr/lib";
        ENV_SSL_CERT_FILE = pythonDir.getAbsolutePath() + "/usr/etc/tls/cert.pem";
        ENV_PYTHONHOME = pythonDir.getAbsolutePath() + "/usr";
        TMPDIR = appContext.getCacheDir().getAbsolutePath();

        initPython(appContext, pythonDir);
        init_ytdlp(appContext, ytdlpDir);
        initialized = true;
    }

    public void init_ytdlp(Context appContext, File ytdlpDir) throws YoutubeDLException {
        if (!ytdlpDir.exists()) ytdlpDir.mkdirs();
        File ytdlpBinary = new File(ytdlpDir, ytdlpBin);
        if (!ytdlpBinary.exists()) {
            try {
                InputStream inputStream = appContext.getResources().openRawResource(R.raw.ytdlp);
                FileUtils.copyInputStreamToFile(inputStream, ytdlpBinary);
            } catch (Exception e) {
                FileUtils.deleteQuietly(ytdlpDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
    }

    public void initPython(Context appContext, File pythonDir) throws YoutubeDLException {
        File pythonLib = new File(binDir, pythonLibName);
        // using size of lib as version
        String pythonSize = String.valueOf(pythonLib.length());
        if (!pythonDir.exists() || shouldUpdatePython(appContext, pythonSize)) {
            FileUtils.deleteQuietly(pythonDir);
            pythonDir.mkdirs();
            try {
                ZipUtils.unzip(pythonLib, pythonDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(pythonDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
            updatePython(appContext, pythonSize);
        }
    }

    private boolean shouldUpdatePython(Context appContext, String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, pythonLibVersion));
    }

    private void updatePython(Context appContext, String version) {
        SharedPrefsHelper.update(appContext, pythonLibVersion, version);
    }

    private void assertInit() {
        if (!initialized) throw new IllegalStateException("instance not initialized");
    }

    // endregion

    // region Info

    public VideoInfo getInfo(String url)
            throws YoutubeDLException, InterruptedException, CanceledException {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        return getInfo(request);
    }

    public VideoInfo getInfo(YoutubeDLRequest request)
            throws YoutubeDLException, InterruptedException, CanceledException {
        request.addOption("--dump-json");
        YoutubeDLResponse response = execute(request, null, false, null);
        VideoInfo videoInfo;
        try {
            videoInfo = objectMapper.readValue(response.getOut(), VideoInfo.class);
        } catch (IOException e) {
            throw new YoutubeDLException("Unable to parse video information", e);
        }
        if (videoInfo == null) throw new YoutubeDLException("Failed to fetch video information");
        return videoInfo;
    }

    // endregion

    // region Process Management

    public boolean destroyProcessById(String id) {
        if (idProcessMap.containsKey(id)) {
            Process p = idProcessMap.get(id);
            boolean alive = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                alive = p.isAlive();
            }
            if (alive) {
                destroyChildProcesses(id);
                p.destroy();
                idProcessMap.remove(id);
                return true;
            }
        }
        return false;
    }

    private boolean destroyChildProcesses(String id) {
        try {
            String command = "pstree -p " + id + " | grep -oP '\\(\\K[^\\)]+' | xargs kill";
            ProcessBuilder processBuilder = new ProcessBuilder("/system/bin/sh", "-c", command);
            Process process = processBuilder.start();
            int res = process.waitFor();
            return res == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // endregion

    // region Execute

    public YoutubeDLResponse execute(YoutubeDLRequest request)
            throws YoutubeDLException, InterruptedException, CanceledException {
        return executeImpl(request, null, false, null);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, String processId)
            throws YoutubeDLException, InterruptedException, CanceledException {
        return executeImpl(request, processId, false, null);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, String processId,
                                     DownloadProgressCallback callback)
            throws YoutubeDLException, InterruptedException, CanceledException {
        return executeImpl(request, processId, false, callback);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, String processId,
                                     boolean redirectErrorStream, DownloadProgressCallback callback)
            throws YoutubeDLException, InterruptedException, CanceledException {
        return executeImpl(request, processId, redirectErrorStream, callback);
    }

    private YoutubeDLResponse executeImpl(YoutubeDLRequest request, String processId,
                                          boolean redirectErrorStream,
                                          DownloadProgressCallback callback)
            throws YoutubeDLException, InterruptedException, CanceledException {
        assertInit();

        if (processId != null && idProcessMap.containsKey(processId)) {
            throw new YoutubeDLException("Process ID already exists");
        }

        // disable caching unless explicitly requested
        if (!request.hasOption("--cache-dir") || request.getOption("--cache-dir") == null) {
            request.addOption("--no-cache-dir");
        }

        if (request.buildCommand().contains("libaria2c.so")) {
            request.addOption("--external-downloader-args", "aria2c:--summary-interval=1")
                   .addOption("--external-downloader-args",
                           "aria2c:--ca-certificate=" + ENV_SSL_CERT_FILE);
        }

        request.addOption("--js-runtimes", "quickjs:" + quickJsPath.getAbsolutePath());

        /* Set ffmpeg location, See https://github.com/xibr/ytdlp-lazy/issues/1 */
        request.addOption("--ffmpeg-location", ffmpegPath.getAbsolutePath());

        StringBuffer outBuffer = new StringBuffer(); // stdout
        StringBuffer errBuffer = new StringBuffer(); // stderr
        long startTime = System.currentTimeMillis();

        List<String> args = request.buildCommand();
        List<String> command = new ArrayList<>();
        command.add(pythonPath.getAbsolutePath());
        command.add(ytdlpPath.getAbsolutePath());
        command.addAll(args);

        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .redirectErrorStream(redirectErrorStream);

        Map<String, String> env = processBuilder.environment();
        env.put("LD_LIBRARY_PATH", ENV_LD_LIBRARY_PATH);
        env.put("SSL_CERT_FILE", ENV_SSL_CERT_FILE);
        env.put("PATH", System.getenv("PATH") + ":" + binDir.getAbsolutePath());
        env.put("PYTHONHOME", ENV_PYTHONHOME);
        env.put("HOME", ENV_PYTHONHOME);
        env.put("TMPDIR", TMPDIR);

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new YoutubeDLException(e);
        }

        if (processId != null) {
            idProcessMap.put(processId, process);
        }

        StreamProcessExtractor stdOutProcessor =
                new StreamProcessExtractor(outBuffer, process.getInputStream(), callback);
        StreamGobbler stdErrProcessor =
                new StreamGobbler(errBuffer, process.getErrorStream());

        int exitCode;
        try {
            stdOutProcessor.join();
            stdErrProcessor.join();
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
            if (processId != null) idProcessMap.remove(processId);
            throw e;
        }

        String out = outBuffer.toString();
        String err = errBuffer.toString();

        if (exitCode > 0) {
            if (processId != null && !idProcessMap.containsKey(processId)) {
                throw new CanceledException();
            }
            if (!ignoreErrors(request, out)) {
                idProcessMap.remove(processId);
                throw new YoutubeDLException(err);
            }
        }

        idProcessMap.remove(processId);

        long elapsedTime = System.currentTimeMillis() - startTime;
        return new YoutubeDLResponse(command, exitCode, elapsedTime, out, err);
    }

    private boolean ignoreErrors(YoutubeDLRequest request, String out) {
        return request.hasOption("--dump-json")
                && !out.isEmpty()
                && request.hasOption("--ignore-errors");
    }

    // endregion

    // region Update

    public synchronized UpdateStatus updateYoutubeDL(Context appContext)
            throws YoutubeDLException {
        return updateYoutubeDL(appContext, UpdateChannel.STABLE);
    }

    public synchronized UpdateStatus updateYoutubeDL(Context appContext,
                                                      UpdateChannel updateChannel)
            throws YoutubeDLException {
        assertInit();
        try {
            return YoutubeDLUpdater.update(appContext, updateChannel);
        } catch (IOException e) {
            throw new YoutubeDLException("failed to update youtube-dl", e);
        }
    }

    public String version(Context appContext) {
        return YoutubeDLUpdater.version(appContext);
    }

    public String versionName(Context appContext) {
        return YoutubeDLUpdater.versionName(appContext);
    }

    // endregion

    // region Inner Classes / Enums

    public static class CanceledException extends Exception {
        public CanceledException() {
            super();
        }
    }

    public enum UpdateStatus {
        DONE,
        ALREADY_UP_TO_DATE
    }

    public static class UpdateChannel {
        public final String apiUrl;

        public UpdateChannel(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public static final UpdateChannel STABLE = new UpdateChannel(
                "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest");
        public static final UpdateChannel NIGHTLY = new UpdateChannel(
                "https://api.github.com/repos/yt-dlp/yt-dlp-nightly-builds/releases/latest");
        public static final UpdateChannel MASTER = new UpdateChannel(
                "https://api.github.com/repos/yt-dlp/yt-dlp-master-builds/releases/latest");
    }

    // endregion
}
