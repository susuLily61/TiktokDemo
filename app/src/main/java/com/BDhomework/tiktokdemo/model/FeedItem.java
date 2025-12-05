package com.BDhomework.tiktokdemo.model;

import java.io.Serializable;

public class FeedItem implements Serializable {
    private final String id;
    private final String title;
    private final String authorName;
    private final String avatarUrl;
    private final String coverUrl;
    private final String videoUrl;
    private final int likeCount;
    private final String description;

    public FeedItem(String id, String title, String authorName, String avatarUrl, String coverUrl, String videoUrl, int likeCount, String description) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.avatarUrl = avatarUrl;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.likeCount = likeCount;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public String getDescription() {
        return description;
    }
}
