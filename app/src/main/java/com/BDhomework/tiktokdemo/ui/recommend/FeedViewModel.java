package com.BDhomework.tiktokdemo.ui.recommend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.BDhomework.tiktokdemo.data.repository.FeedRepository;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private final FeedRepository repository;
    private final MutableLiveData<FeedUiState> uiState = new MutableLiveData<>(FeedUiState.initial());
    private int currentPage = 0;

    public FeedViewModel(FeedRepository repository) {
        this.repository = repository;
    }

    public LiveData<FeedUiState> getUiState() {
        return uiState;
    }

    public void refresh() {
        currentPage = 0;
        FeedUiState current = getCurrentState();
        uiState.setValue(new FeedUiState(current.getItems(), true, false, null));

        try {
            List<FeedItem> data = repository.loadFeed(currentPage);
            uiState.setValue(new FeedUiState(data, false, false, null));
        } catch (Exception e) {
            uiState.setValue(new FeedUiState(current.getItems(), false, false, e.getMessage()));
        }
    }

    public void loadMore() {
        FeedUiState current = getCurrentState();
        if (current.isLoadingMore()) {
            return;
        }

        int nextPage = currentPage + 1;
        uiState.setValue(new FeedUiState(current.getItems(), current.isRefreshing(), true, null));

        try {
            List<FeedItem> more = repository.loadFeed(nextPage);
            List<FeedItem> merged = new ArrayList<>(current.getItems());
            merged.addAll(more);
            currentPage = nextPage;
            uiState.setValue(new FeedUiState(merged, false, false, null));
        } catch (Exception e) {
            uiState.setValue(new FeedUiState(current.getItems(), false, false, e.getMessage()));
        }
    }

    private FeedUiState getCurrentState() {
        FeedUiState state = uiState.getValue();
        if (state == null) {
            state = FeedUiState.initial();
        }
        return state;
    }
}
