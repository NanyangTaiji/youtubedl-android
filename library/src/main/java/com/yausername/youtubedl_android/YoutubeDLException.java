package com.yausername.youtubedl_android;

public class YoutubeDLException extends Exception {

    public YoutubeDLException(String message) {
        super(message);
    }

    public YoutubeDLException(String message, Throwable cause) {
        super(message, cause);
    }

    public YoutubeDLException(Throwable cause) {
        super(cause);
    }
}
