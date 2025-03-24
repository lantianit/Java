package org.example.entity.VO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewVideoVO {
    private int videoId;

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public void setIsRecommend(int isRecommend) {
        this.isRecommend = isRecommend;
    }

    private String title;
    private String duration;
    private String description;
    @JsonProperty("isRecommend")
    private int isRecommend;
    private String type;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(String isRecommend) {
        this.isRecommend = Integer.parseInt(isRecommend);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}