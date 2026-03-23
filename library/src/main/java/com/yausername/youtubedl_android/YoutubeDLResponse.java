package com.yausername.youtubedl_android;

import java.util.List;

public class YoutubeDLResponse {

    private final List<String> command;
    private final int exitCode;
    private final long elapsedTime;
    private final String out;
    private final String err;

    public YoutubeDLResponse(List<String> command, int exitCode, long elapsedTime,
                             String out, String err) {
        this.command = command;
        this.exitCode = exitCode;
        this.elapsedTime = elapsedTime;
        this.out = out;
        this.err = err;
    }

    public List<String> getCommand() {
        return command;
    }

    public int getExitCode() {
        return exitCode;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }
}
