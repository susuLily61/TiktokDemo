package com.BDhomework.tiktokdemo.player;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * UI state for the video feed screen.
 */
public class VideoFeedUiState {

    private final List<FeedItem> items;
    private final int currentIndex;
    private final boolean isMuted;
    private final String errorMsg;
    private final int preloadRadius;
    private final Map<String, FeedInteractionState> feedStates;

    public VideoFeedUiState(List<FeedItem> items,
                            int currentIndex,
                            boolean isMuted,
                            String errorMsg,
                            int preloadRadius,
                            Map<String, FeedInteractionState> feedStates) {
        this.items = items;
        this.currentIndex = currentIndex;
        this.isMuted = isMuted;
        this.errorMsg = errorMsg;
        this.preloadRadius = preloadRadius;
        this.feedStates = feedStates;
    }

    public List<FeedItem> getItems() {
        return items;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getPreloadRadius() {
        return preloadRadius;
    }

    public Map<String, FeedInteractionState> getFeedStates() {
        return Collections.unmodifiableMap(feedStates);
    }

    public FeedInteractionState getFeedState(String feedId) {
        return feedStates.get(feedId);
    }

    public static class FeedInteractionState {
        private boolean liked;
        private boolean collected;
        private final int baseLikeCount;
        private int baseCommentCount;
        private int baseCollectCount;
        private int baseShareCount;

        public FeedInteractionState(boolean liked,
                                    boolean collected,
                                    int baseLikeCount,
                                    int baseCommentCount,
                                    int baseCollectCount,
                                    int baseShareCount) {
            this.liked = liked;
            this.collected = collected;
            this.baseLikeCount = baseLikeCount;
            this.baseCommentCount = baseCommentCount;
            this.baseCollectCount = baseCollectCount;
            this.baseShareCount = baseShareCount;
        }

        public FeedInteractionState(FeedInteractionState other) {
            this.liked = other.liked;
            this.collected = other.collected;
            this.baseLikeCount = other.baseLikeCount;
            this.baseCommentCount = other.baseCommentCount;
            this.baseCollectCount = other.baseCollectCount;
            this.baseShareCount = other.baseShareCount;
        }

        public boolean isLiked() {
            return liked;
        }

        public boolean isCollected() {
            return collected;
        }

        public int getBaseLikeCount() {
            return baseLikeCount;
        }

        public int getBaseCommentCount() {
            return baseCommentCount;
        }

        public int getBaseCollectCount() {
            return baseCollectCount;
        }

        public int getBaseShareCount() {
            return baseShareCount;
        }

        public void setLiked(boolean liked) {
            this.liked = liked;
        }

        public void setCollected(boolean collected) {
            this.collected = collected;
        }

        public void setBaseCommentCount(int baseCommentCount) {
            this.baseCommentCount = baseCommentCount;
        }

        public void setBaseCollectCount(int baseCollectCount) {
            this.baseCollectCount = baseCollectCount;
        }

        public void setBaseShareCount(int baseShareCount) {
            this.baseShareCount = baseShareCount;
        }
    }
}
