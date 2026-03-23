package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoFormat {
    private int asr = 0;
    private int tbr = 0;
    private int abr = 0;
    private String format = null;

    @JsonProperty("format_id")
    private String formatId = null;

    @JsonProperty("format_note")
    private String formatNote = null;

    private String ext = null;
    private int preference = 0;
    private String vcodec = null;
    private String acodec = null;
    private int width = 0;
    private int height = 0;

    @JsonProperty("filesize")
    private long fileSize = 0;

    @JsonProperty("filesize_approx")
    private long fileSizeApproximate = 0;

    private int fps = 0;
    private String url = null;

    @JsonProperty("manifest_url")
    private String manifestUrl = null;

    @JsonProperty("http_headers")
    private Map<String, String> httpHeaders = null;

    // Getters and Setters
    public int getAsr() { return asr; }
    public void setAsr(int asr) { this.asr = asr; }

    public int getTbr() { return tbr; }
    public void setTbr(int tbr) { this.tbr = tbr; }

    public int getAbr() { return abr; }
    public void setAbr(int abr) { this.abr = abr; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getFormatId() { return formatId; }
    public void setFormatId(String formatId) { this.formatId = formatId; }

    public String getFormatNote() { return formatNote; }
    public void setFormatNote(String formatNote) { this.formatNote = formatNote; }

    public String getExt() { return ext; }
    public void setExt(String ext) { this.ext = ext; }

    public int getPreference() { return preference; }
    public void setPreference(int preference) { this.preference = preference; }

    public String getVcodec() { return vcodec; }
    public void setVcodec(String vcodec) { this.vcodec = vcodec; }

    public String getAcodec() { return acodec; }
    public void setAcodec(String acodec) { this.acodec = acodec; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getFileSizeApproximate() { return fileSizeApproximate; }
    public void setFileSizeApproximate(long fileSizeApproximate) { this.fileSizeApproximate = fileSizeApproximate; }

    public int getFps() { return fps; }
    public void setFps(int fps) { this.fps = fps; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getManifestUrl() { return manifestUrl; }
    public void setManifestUrl(String manifestUrl) { this.manifestUrl = manifestUrl; }

    public Map<String, String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(Map<String, String> httpHeaders) { this.httpHeaders = httpHeaders; }
}