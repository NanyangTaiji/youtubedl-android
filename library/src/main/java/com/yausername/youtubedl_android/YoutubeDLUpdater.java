package com.yausername.youtubedl_android;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel;
import com.yausername.youtubedl_android.YoutubeDL.UpdateStatus;
import com.yausername.youtubedl_android.utils.SharedPrefsHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

class YoutubeDLUpdater {

    private static final String dlpBinaryName = "yt-dlp";
    private static final String dlpVersionKey = "dlpVersion";
    private static final String dlpVersionNameKey = "dlpVersionName";

    private YoutubeDLUpdater() {}

    static UpdateStatus update(Context appContext, UpdateChannel youtubeDLChannel)
            throws IOException, YoutubeDLException {
        JsonNode json = checkForUpdate(appContext, youtubeDLChannel);
        if (json == null) return UpdateStatus.ALREADY_UP_TO_DATE;

        String downloadUrl = getDownloadUrl(json);
        File file = download(appContext, downloadUrl);
        File ytdlpDir = getYoutubeDLDir(appContext);
        File binary = new File(ytdlpDir, dlpBinaryName);

        try {
            /* purge older version */
            if (ytdlpDir.exists()) FileUtils.deleteDirectory(ytdlpDir);
            /* install newer version */
            ytdlpDir.mkdirs();
            FileUtils.copyFile(file, binary);
        } catch (Exception e) {
            /* if something went wrong restore default version */
            FileUtils.deleteQuietly(ytdlpDir);
            YoutubeDL.getInstance().init_ytdlp(appContext, ytdlpDir);
            throw new YoutubeDLException(e);
        } finally {
            file.delete();
        }

        updateSharedPrefs(appContext, getTag(json), getName(json));
        return UpdateStatus.DONE;
    }

    private static void updateSharedPrefs(Context appContext, String tag, String name) {
        SharedPrefsHelper.update(appContext, dlpVersionKey, tag);
        SharedPrefsHelper.update(appContext, dlpVersionNameKey, name);
    }

    private static JsonNode checkForUpdate(Context appContext, UpdateChannel youtubeDLChannel)
            throws IOException {
        URL url = new URL(youtubeDLChannel.apiUrl);
        JsonNode json = YoutubeDL.objectMapper.readTree(url);
        String newVersion = getTag(json);
        String oldVersion = SharedPrefsHelper.get(appContext, dlpVersionKey);
        if (newVersion.equals(oldVersion)) return null;
        return json;
    }

    private static String getTag(JsonNode json) {
        return json.get("tag_name").asText();
    }

    private static String getName(JsonNode json) {
        return json.get("name").asText();
    }

    private static String getDownloadUrl(JsonNode json) throws YoutubeDLException {
        ArrayNode assets = (ArrayNode) json.get("assets");
        String downloadUrl = "";
        for (JsonNode asset : assets) {
            if (YoutubeDL.ytdlpBin.equals(asset.get("name").asText())) {
                downloadUrl = asset.get("browser_download_url").asText();
                break;
            }
        }
        if (downloadUrl.isEmpty()) throw new YoutubeDLException("unable to get download url");
        return downloadUrl;
    }

    private static File download(Context appContext, String url) throws IOException {
        URL downloadUrl = new URL(url);
        File file = File.createTempFile(dlpBinaryName, null, appContext.getCacheDir());
        FileUtils.copyURLToFile(downloadUrl, file, 5000, 10000);
        return file;
    }

    private static File getYoutubeDLDir(Context appContext) {
        File baseDir = new File(appContext.getNoBackupFilesDir(), YoutubeDL.baseName);
        return new File(baseDir, YoutubeDL.ytdlpDirName);
    }

    static String version(Context appContext) {
        return SharedPrefsHelper.get(appContext, dlpVersionKey);
    }

    static String versionName(Context appContext) {
        return SharedPrefsHelper.get(appContext, dlpVersionNameKey);
    }
}
