package com.yausername.youtubedl_android;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

class StreamGobbler extends Thread {

    private static final String TAG = StreamGobbler.class.getSimpleName();

    private final StringBuffer buffer;
    private final InputStream stream;

    StreamGobbler(StringBuffer buffer, InputStream stream) {
        this.buffer = buffer;
        this.stream = stream;
        start();
    }

    @Override
    public void run() {
        try {
            Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                buffer.append((char) nextChar);
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to read stream", e);
        }
    }
}
