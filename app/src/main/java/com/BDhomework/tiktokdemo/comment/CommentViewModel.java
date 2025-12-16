package com.BDhomework.tiktokdemo.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.BDhomework.tiktokdemo.data.repository.CommentPage;
import com.BDhomework.tiktokdemo.data.repository.FeedRepository;
import com.BDhomework.tiktokdemo.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentViewModel extends ViewModel {

    private static final int PAGE_SIZE = 5;

    private final FeedRepository repository;
    private final MutableLiveData<CommentUiState> uiState = new MutableLiveData<>(CommentUiState.initial());
    private int currentPage = -1;

    public CommentViewModel(FeedRepository repository) {
        this.repository = repository;
    }

    public LiveData<CommentUiState> getUiState() {
        return uiState;
    }

    public void loadInitial(String feedId) {
        currentPage = -1;
        loadMore(feedId);
    }

    public void loadMore(String feedId) {
        CommentUiState current = getCurrentState();
        if (current.isLoading() || (!current.hasMore() && currentPage >= 0)) {
            return;
        }

        uiState.setValue(new CommentUiState(current.getComments(), true, null, current.hasMore()));
        int nextPage = currentPage + 1;

        try {
            CommentPage page = repository.loadComments(feedId, nextPage, PAGE_SIZE);
            List<Comment> merged = new ArrayList<>(current.getComments());
            merged.addAll(page.getComments());
            currentPage = nextPage;
            uiState.setValue(new CommentUiState(merged, false, null, page.hasMore()));
        } catch (Exception e) {
            uiState.setValue(new CommentUiState(current.getComments(), false, e.getMessage(), current.hasMore()));
        }
    }

    public void send(String feedId, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        CommentUiState current = getCurrentState();
        try {
            Comment newComment = repository.sendComment(feedId, trimmed);
            List<Comment> updated = new ArrayList<>();
            updated.add(newComment);
            updated.addAll(current.getComments());
            uiState.setValue(new CommentUiState(updated, false, null, current.hasMore()));
        } catch (Exception e) {
            uiState.setValue(current.withError(e.getMessage()));
        }
    }

    private CommentUiState getCurrentState() {
        CommentUiState state = uiState.getValue();
        if (state == null) {
            state = CommentUiState.initial();
        }
        return state;
    }
}
