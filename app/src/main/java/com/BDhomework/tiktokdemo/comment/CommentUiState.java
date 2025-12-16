package com.BDhomework.tiktokdemo.comment;

import com.BDhomework.tiktokdemo.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentUiState {
    private final List<Comment> comments;
    private final boolean loading;
    private final String error;
    private final boolean hasMore;

    public CommentUiState(List<Comment> comments, boolean loading, String error, boolean hasMore) {
        this.comments = Collections.unmodifiableList(new ArrayList<>(comments));
        this.loading = loading;
        this.error = error;
        this.hasMore = hasMore;
    }

    public static CommentUiState initial() {
        return new CommentUiState(Collections.emptyList(), false, null, true);
    }

    public List<Comment> getComments() {
        return comments;
    }

    public boolean isLoading() {
        return loading;
    }

    public String getError() {
        return error;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public CommentUiState withLoading(boolean loading) {
        return new CommentUiState(comments, loading, error, hasMore);
    }

    public CommentUiState withError(String message) {
        return new CommentUiState(comments, loading, message, hasMore);
    }
}
