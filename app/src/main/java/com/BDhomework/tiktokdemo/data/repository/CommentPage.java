package com.BDhomework.tiktokdemo.data.repository;

import com.BDhomework.tiktokdemo.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentPage {
    private final List<Comment> comments;
    private final boolean hasMore;

    public CommentPage(List<Comment> comments, boolean hasMore) {
        this.comments = Collections.unmodifiableList(new ArrayList<>(comments));
        this.hasMore = hasMore;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
