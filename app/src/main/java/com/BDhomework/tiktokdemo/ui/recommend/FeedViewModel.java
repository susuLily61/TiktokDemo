package com.BDhomework.tiktokdemo.ui.recommend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.BDhomework.tiktokdemo.data.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private final MockFeedRepository repository = new MockFeedRepository();
    private final MutableLiveData<List<FeedItem>> feedLiveData = new MutableLiveData<>(new ArrayList<>());
    private int currentPage = 0;

    public LiveData<List<FeedItem>> getFeedLiveData() {
        return feedLiveData;
    }

    public void loadInitial() {
        currentPage = 0;
        List<FeedItem> data = repository.loadFeed(currentPage);
        feedLiveData.setValue(data);
    }

    public void loadMore() {
        currentPage += 1;
        List<FeedItem> more = repository.loadFeed(currentPage);
        List<FeedItem> current = new ArrayList<>(feedLiveData.getValue());
        current.addAll(more);
        feedLiveData.setValue(current);
    }

    public void refresh() {
        loadInitial();
    }
}
