package com.BDhomework.tiktokdemo.model;

public class Comment {
    private final String user;
    private final String content;

    public Comment(String user, String content) {
        this.user = user;
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
}
