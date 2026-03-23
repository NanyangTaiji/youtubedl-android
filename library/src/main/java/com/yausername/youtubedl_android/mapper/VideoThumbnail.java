package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoThumbnail {
    private String url = null;
    private String id = null;

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}