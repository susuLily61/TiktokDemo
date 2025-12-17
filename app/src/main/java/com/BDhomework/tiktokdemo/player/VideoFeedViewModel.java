package com.BDhomework.tiktokdemo.player;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VideoFeedViewModel extends ViewModel {

    private final MutableLiveData<VideoFeedUiState> uiState = new MutableLiveData<>();
    private final Random random = new Random();
    private final List<FeedItem> items;

    public VideoFeedViewModel(List<FeedItem> items, int startIndex) {
        this.items = new ArrayList<>(items);
        Map<String, VideoFeedUiState.FeedInteractionState> feedStates = new HashMap<>();
        for (FeedItem item : items) {
            String id = item.getId();
            feedStates.put(id, buildInitialInteraction(item));
        }
        uiState.setValue(new VideoFeedUiState(
                new ArrayList<>(items),
                startIndex,
                false,
                null,
                1,
                feedStates
        ));
    }

    public LiveData<VideoFeedUiState> getUiState() {
        return uiState;
    }

    public List<FeedItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getCurrentIndex() {
        VideoFeedUiState state = uiState.getValue();
        return state != null ? state.getCurrentIndex() : 0;
    }

    public void onPageSelected(int position) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null || position == state.getCurrentIndex()) return;
        uiState.setValue(new VideoFeedUiState(
                state.getItems(),
                position,
                state.isMuted(),
                state.getErrorMsg(),
                state.getPreloadRadius(),
                new HashMap<>(state.getFeedStates())
        ));
    }

    public void setMuted(boolean muted) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null || muted == state.isMuted()) return;
        uiState.setValue(new VideoFeedUiState(
                state.getItems(),
                state.getCurrentIndex(),
                muted,
                state.getErrorMsg(),
                state.getPreloadRadius(),
                new HashMap<>(state.getFeedStates())
        ));
    }

    public void setPreloadRadius(int preloadRadius) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return;
        uiState.setValue(new VideoFeedUiState(
                state.getItems(),
                state.getCurrentIndex(),
                state.isMuted(),
                state.getErrorMsg(),
                preloadRadius,
                new HashMap<>(state.getFeedStates())
        ));
    }

    public void toggleLike(String feedId) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return;
        Map<String, VideoFeedUiState.FeedInteractionState> feedStates = copyFeedStates(state.getFeedStates());
        VideoFeedUiState.FeedInteractionState interaction = ensureFeedState(feedStates, feedId);
        interaction.setLiked(!interaction.isLiked());
        emitStateWithFeedMap(state, feedStates);
    }

    public void setLiked(String feedId, boolean liked) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return;
        Map<String, VideoFeedUiState.FeedInteractionState> feedStates = copyFeedStates(state.getFeedStates());
        VideoFeedUiState.FeedInteractionState interaction = ensureFeedState(feedStates, feedId);
        if (interaction.isLiked() == liked) return;
        interaction.setLiked(liked);
        emitStateWithFeedMap(state, feedStates);
    }

    public void toggleCollect(String feedId) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return;
        Map<String, VideoFeedUiState.FeedInteractionState> feedStates = copyFeedStates(state.getFeedStates());
        VideoFeedUiState.FeedInteractionState interaction = ensureFeedState(feedStates, feedId);
        interaction.setCollected(!interaction.isCollected());
        emitStateWithFeedMap(state, feedStates);
    }

    public void updateCommentCount(String feedId, int newTotal) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return;
        Map<String, VideoFeedUiState.FeedInteractionState> feedStates = copyFeedStates(state.getFeedStates());
        VideoFeedUiState.FeedInteractionState interaction = ensureFeedState(feedStates, feedId);
        interaction.setBaseCommentCount(newTotal);
        emitStateWithFeedMap(state, feedStates);
    }

    public int getDisplayLikeCount(String feedId, int fallback) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return fallback;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        if (interaction == null) return fallback;
        return interaction.getBaseLikeCount() + (interaction.isLiked() ? 1 : 0);
    }

    public int getDisplayCollectCount(String feedId, int fallback) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return fallback;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        if (interaction == null) return fallback;
        return interaction.getBaseCollectCount() + (interaction.isCollected() ? 1 : 0);
    }

    public int getCommentCount(String feedId, int fallback) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return fallback;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        if (interaction == null) return fallback;
        return interaction.getBaseCommentCount();
    }

    public int getShareCount(String feedId, int fallback) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return fallback;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        if (interaction == null) return fallback;
        return interaction.getBaseShareCount();
    }

    public boolean isLiked(String feedId) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return false;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        return interaction != null && interaction.isLiked();
    }

    public boolean isCollected(String feedId) {
        VideoFeedUiState state = uiState.getValue();
        if (state == null) return false;
        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedId);
        return interaction != null && interaction.isCollected();
    }

    private VideoFeedUiState.FeedInteractionState ensureFeedState(Map<String, VideoFeedUiState.FeedInteractionState> map, String feedId) {
        VideoFeedUiState.FeedInteractionState state = map.get(feedId);
        if (state != null) return state;
        FeedItem fallbackItem = null;
        for (FeedItem item : items) {
            if (item.getId().equals(feedId)) {
                fallbackItem = item;
                break;
            }
        }
        if (fallbackItem == null) {
            fallbackItem = items.isEmpty() ? null : items.get(0);
        }
        VideoFeedUiState.FeedInteractionState newState = buildInitialInteraction(fallbackItem);
        map.put(feedId, newState);
        return newState;
    }

    private Map<String, VideoFeedUiState.FeedInteractionState> copyFeedStates(Map<String, VideoFeedUiState.FeedInteractionState> original) {
        Map<String, VideoFeedUiState.FeedInteractionState> copy = new HashMap<>();
        for (Map.Entry<String, VideoFeedUiState.FeedInteractionState> entry : original.entrySet()) {
            copy.put(entry.getKey(), new VideoFeedUiState.FeedInteractionState(entry.getValue()));
        }
        return copy;
    }

    private void emitStateWithFeedMap(VideoFeedUiState state, Map<String, VideoFeedUiState.FeedInteractionState> feedStates) {
        uiState.setValue(new VideoFeedUiState(
                state.getItems(),
                state.getCurrentIndex(),
                state.isMuted(),
                state.getErrorMsg(),
                state.getPreloadRadius(),
                feedStates
        ));
    }

    private VideoFeedUiState.FeedInteractionState buildInitialInteraction(FeedItem item) {
        if (item == null) {
            return new VideoFeedUiState.FeedInteractionState(
                    false,
                    false,
                    0,
                    0,
                    0,
                    0
            );
        }
        return new VideoFeedUiState.FeedInteractionState(
                false,
                false,
                item.getLikeCount(),
                80 + random.nextInt(140),
                50 + random.nextInt(120),
                random.nextInt(200)
        );
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final List<FeedItem> items;
        private final int startIndex;

        public Factory(List<FeedItem> items, int startIndex) {
            this.items = items;
            this.startIndex = startIndex;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new VideoFeedViewModel(items, startIndex);
        }
    }
}
