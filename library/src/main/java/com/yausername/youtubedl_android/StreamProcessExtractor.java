package com.yausername.youtubedl_android;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StreamProcessExtractor extends Thread {

    private static final String TAG = StreamProcessExtractor.class.getSimpleName();
    private static final long ETA = -1L;
    private static final float PERCENT = -1.0f;
    private static final int GROUP_PERCENT = 1;
    private static final int GROUP_MINUTES = 2;
    private static final int GROUP_SECONDS = 3;

    private final StringBuffer buffer;
    private final InputStream stream;
    private final DownloadProgressCallback callback;

    private final Pattern p =
            Pattern.compile("\\[download\\]\\s+(\\d+\\.\\d)% .* ETA (\\d+):(\\d+)");
    private final Pattern pAria2c =
            Pattern.compile("\\[#\\w{6}.*\\((\\d*\\.*\\d+)%\\).*?((\\d+)m)*((\\d+)s)*]");
    private final Pattern pFFmpeg =
            Pattern.compile("size=.*");

    private float progress = PERCENT;
    private long eta = ETA;

    StreamProcessExtractor(StringBuffer buffer, InputStream stream,
                           DownloadProgressCallback callback) {
        this.buffer = buffer;
        this.stream = stream;
        this.callback = callback;
        start();
    }

    @Override
    public void run() {
        try {
            Reader input = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringBuilder currentLine = new StringBuilder();
            int nextChar;
            while ((nextChar = input.read()) != -1) {
                buffer.append((char) nextChar);
                if (nextChar == '\r' || (nextChar == '\n' && callback != null)) {
                    processOutputLine(currentLine.toString());
                    currentLine.setLength(0);
                    continue;
                }
                currentLine.append((char) nextChar);
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to read stream", e);
        }
    }

    private void processOutputLine(String line) {
        if (callback != null) {
            callback.onProgressUpdate(getProgress(line), getEta(line), line);
        }
    }

    private float getProgress(String line) {
        Matcher matcher = p.matcher(line);
        if (matcher.find()) {
            progress = Float.parseFloat(matcher.group(GROUP_PERCENT));
            return progress;
        }

        Matcher mAria2c = pAria2c.matcher(line);
        if (mAria2c.find()) {
            progress = Float.parseFloat(mAria2c.group(1));
            return progress;
        }

        Matcher mFFmpeg = pFFmpeg.matcher(line);
        if (mFFmpeg.find()) {
            progress = 99f;
            return progress;
        }

        return progress;
    }

    private long getEta(String line) {
        Matcher matcher = p.matcher(line);
        if (matcher.find()) {
            eta = convertToSeconds(matcher.group(GROUP_MINUTES), matcher.group(GROUP_SECONDS));
            return eta;
        }

        Matcher mAria2c = pAria2c.matcher(line);
        if (mAria2c.find()) {
            eta = convertToSeconds(mAria2c.group(3), mAria2c.group(5));
            return eta;
        }

        return eta;
    }

    private int convertToSeconds(String minutes, String seconds) {
        if (seconds == null) return 0;
        if (minutes == null) return Integer.parseInt(seconds);
        return Integer.parseInt(minutes) * 60 + Integer.parseInt(seconds);
    }
}
