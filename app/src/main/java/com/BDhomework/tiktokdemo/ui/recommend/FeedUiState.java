package com.BDhomework.tiktokdemo.ui.recommend;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedUiState {
    private final List<FeedItem> items;
    private final boolean refreshing;
    private final boolean loadingMore;
    private final String errorMessage;

    public FeedUiState(List<FeedItem> items, boolean refreshing, boolean loadingMore, String errorMessage) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.refreshing = refreshing;
        this.loadingMore = loadingMore;
        this.errorMessage = errorMessage;
    }

    public static FeedUiState initial() {
        return new FeedUiState(Collections.emptyList(), false, false, null);
    }

    public List<FeedItem> getItems() {
        return items;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public boolean isLoadingMore() {
        return loadingMore;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public FeedUiState withRefreshing(boolean refreshing) {
        return new FeedUiState(items, refreshing, loadingMore, errorMessage);
    }

    public FeedUiState withLoadingMore(boolean loadingMore) {
        return new FeedUiState(items, refreshing, loadingMore, errorMessage);
    }

    public FeedUiState withError(String message) {
        return new FeedUiState(items, refreshing, loadingMore, message);
    }
}
