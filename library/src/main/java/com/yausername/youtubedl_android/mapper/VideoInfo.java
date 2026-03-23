package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoInfo {
    private String id = null;
    private String fulltitle = null;
    private String title = null;

    @JsonProperty("upload_date")
    private String uploadDate = null;

    @JsonProperty("display_id")
    private String displayId = null;

    private int duration = 0;
    private String description = null;
    private String thumbnail = null;
    private String license = null;
    private String extractor = null;

    @JsonProperty("extractor_key")
    private String extractorKey = null;

    @JsonProperty("view_count")
    private String viewCount = null;

    @JsonProperty("like_count")
    private String likeCount = null;

    @JsonProperty("dislike_count")
    private String dislikeCount = null;

    @JsonProperty("repost_count")
    private String repostCount = null;

    @JsonProperty("average_rating")
    private String averageRating = null;

    @JsonProperty("uploader_id")
    private String uploaderId = null;

    private String uploader = null;

    @JsonProperty("player_url")
    private String playerUrl = null;

    @JsonProperty("webpage_url")
    private String webpageUrl = null;

    @JsonProperty("webpage_url_basename")
    private String webpageUrlBasename = null;

    private String resolution = null;
    private int width = 0;
    private int height = 0;
    private String format = null;

    @JsonProperty("format_id")
    private String formatId = null;

    private String ext = null;

    @JsonProperty("filesize")
    private long fileSize = 0;

    @JsonProperty("filesize_approx")
    private long fileSizeApproximate = 0;

    @JsonProperty("http_headers")
    private Map<String, String> httpHeaders = null;

    private ArrayList<String> categories = null;
    private ArrayList<String> tags = null;

    @JsonProperty("requested_formats")
    private ArrayList<VideoFormat> requestedFormats = null;

    private ArrayList<VideoFormat> formats = null;
    private ArrayList<VideoThumbnail> thumbnails = null;

    @JsonProperty("manifest_url")
    private String manifestUrl = null;

    private String url = null;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFulltitle() { return fulltitle; }
    public void setFulltitle(String fulltitle) { this.fulltitle = fulltitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getDisplayId() { return displayId; }
    public void setDisplayId(String displayId) { this.displayId = displayId; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getExtractor() { return extractor; }
    public void setExtractor(String extractor) { this.extractor = extractor; }

    public String getExtractorKey() { return extractorKey; }
    public void setExtractorKey(String extractorKey) { this.extractorKey = extractorKey; }

    public String getViewCount() { return viewCount; }
    public void setViewCount(String viewCount) { this.viewCount = viewCount; }

    public String getLikeCount() { return likeCount; }
    public void setLikeCount(String likeCount) { this.likeCount = likeCount; }

    public String getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(String dislikeCount) { this.dislikeCount = dislikeCount; }

    public String getRepostCount() { return repostCount; }
    public void setRepostCount(String repostCount) { this.repostCount = repostCount; }

    public String getAverageRating() { return averageRating; }
    public void setAverageRating(String averageRating) { this.averageRating = averageRating; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public String getUploader() { return uploader; }
    public void setUploader(String uploader) { this.uploader = uploader; }

    public String getPlayerUrl() { return playerUrl; }
    public void setPlayerUrl(String playerUrl) { this.playerUrl = playerUrl; }

    public String getWebpageUrl() { return webpageUrl; }
    public void setWebpageUrl(String webpageUrl) { this.webpageUrl = webpageUrl; }

    public String getWebpageUrlBasename() { return webpageUrlBasename; }
    public void setWebpageUrlBasename(String webpageUrlBasename) { this.webpageUrlBasename = webpageUrlBasename; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getFormatId() { return formatId; }
    public void setFormatId(String formatId) { this.formatId = formatId; }

    public String getExt() { return ext; }
    public void setExt(String ext) { this.ext = ext; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getFileSizeApproximate() { return fileSizeApproximate; }
    public void setFileSizeApproximate(long fileSizeApproximate) { this.fileSizeApproximate = fileSizeApproximate; }

    public Map<String, String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(Map<String, String> httpHeaders) { this.httpHeaders = httpHeaders; }

    public ArrayList<String> getCategories() { return categories; }
    public void setCategories(ArrayList<String> categories) { this.categories = categories; }

    public ArrayList<String> getTags() { return tags; }
    public void setTags(ArrayList<String> tags) { this.tags = tags; }

    public ArrayList<VideoFormat> getRequestedFormats() { return requestedFormats; }
    public void setRequestedFormats(ArrayList<VideoFormat> requestedFormats) { this.requestedFormats = requestedFormats; }

    public ArrayList<VideoFormat> getFormats() { return formats; }
    public void setFormats(ArrayList<VideoFormat> formats) { this.formats = formats; }

    public ArrayList<VideoThumbnail> getThumbnails() { return thumbnails; }
    public void setThumbnails(ArrayList<VideoThumbnail> thumbnails) { this.thumbnails = thumbnails; }

    public String getManifestUrl() { return manifestUrl; }
    public void setManifestUrl(String manifestUrl) { this.manifestUrl = manifestUrl; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}