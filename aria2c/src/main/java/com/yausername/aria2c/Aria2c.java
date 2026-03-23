package com.yausername.aria2c;

import android.content.Context;

import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.utils.SharedPrefsHelper;
import com.yausername.youtubedl_android.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public final class Aria2c {

    private static final String BASE_NAME          = "youtubedl-android";
    private static final String PACKAGES_ROOT      = "packages";
    private static final String ARIA2C_DIR_NAME    = "aria2c";
    private static final String ARIA2C_LIB_NAME    = "libaria2c.zip.so";
    private static final String ARIA2C_LIB_VERSION = "aria2cLibVersion";

    private static final Aria2c INSTANCE = new Aria2c();

    private boolean initialized = false;
    private File binDir = null;

    private Aria2c() {}

    public static Aria2c getInstance() {
        return INSTANCE;
    }

    public synchronized void init(Context appContext) {
        if (initialized) return;

        File baseDir = new File(appContext.getNoBackupFilesDir(), BASE_NAME);
        if (!baseDir.exists()) baseDir.mkdir();

        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);

        File packagesDir = new File(baseDir, PACKAGES_ROOT);
        File aria2cDir   = new File(packagesDir, ARIA2C_DIR_NAME);

        initAria2c(appContext, aria2cDir);
        initialized = true;
    }

    private void initAria2c(Context appContext, File aria2cDir) {
        File aria2cLib = new File(binDir, ARIA2C_LIB_NAME);
        if (!aria2cLib.exists()) {
            return;
        }

        // using size of lib as version
        String aria2cSize = String.valueOf(aria2cLib.length());

        if (!aria2cDir.exists() || shouldUpdateAria2c(appContext, aria2cSize)) {
            FileUtils.deleteQuietly(aria2cDir);
            aria2cDir.mkdirs();
            try {
                ZipUtils.unzip(aria2cLib, aria2cDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(aria2cDir);
                try {
                    throw new YoutubeDLException("failed to initialize", e);
                } catch (YoutubeDLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            updateAria2c(appContext, aria2cSize);
        }
    }

    private boolean shouldUpdateAria2c(Context appContext, String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, ARIA2C_LIB_VERSION));
    }

    private void updateAria2c(Context appContext, String version) {
        SharedPrefsHelper.update(appContext, ARIA2C_LIB_VERSION, version);
    }
}
