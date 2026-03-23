package com.yausername.ffmpeg;

import android.content.Context;

import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.utils.SharedPrefsHelper;
import com.yausername.youtubedl_android.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public final class FFmpeg {

    private static final String BASE_NAME        = "youtubedl-android";
    private static final String PACKAGES_ROOT    = "packages";
    private static final String FFMPEG_DIR_NAME  = "ffmpeg";
    private static final String FFMPEG_LIB_NAME  = "libffmpeg.zip.so";
    private static final String FFMPEG_LIB_VERSION = "ffmpegLibVersion";

    private static final FFmpeg INSTANCE = new FFmpeg();

    private boolean initialized = false;
    private File binDir = null;

    private FFmpeg() {}

    public static FFmpeg getInstance() {
        return INSTANCE;
    }

    public synchronized void init(Context appContext) {
        if (initialized) return;

        File baseDir = new File(appContext.getNoBackupFilesDir(), BASE_NAME);
        if (!baseDir.exists()) baseDir.mkdir();

        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);

        File packagesDir = new File(baseDir, PACKAGES_ROOT);
        File ffmpegDir   = new File(packagesDir, FFMPEG_DIR_NAME);

        initFFmpeg(appContext, ffmpegDir);
        initialized = true;
    }

    private void initFFmpeg(Context appContext, File ffmpegDir) {
        File ffmpegLib = new File(binDir, FFMPEG_LIB_NAME);
        // using size of lib as version
        String ffmpegSize = String.valueOf(ffmpegLib.length());

        if (!ffmpegDir.exists() || shouldUpdateFFmpeg(appContext, ffmpegSize)) {
            FileUtils.deleteQuietly(ffmpegDir);
            ffmpegDir.mkdirs();
            try {
                ZipUtils.unzip(ffmpegLib, ffmpegDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(ffmpegDir);
                try {
                    throw new YoutubeDLException("failed to initialize", e);
                } catch (YoutubeDLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            updateFFmpeg(appContext, ffmpegSize);
        }
    }

    private boolean shouldUpdateFFmpeg(Context appContext, String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, FFMPEG_LIB_VERSION));
    }

    private void updateFFmpeg(Context appContext, String version) {
        SharedPrefsHelper.update(appContext, FFMPEG_LIB_VERSION, version);
    }
}
