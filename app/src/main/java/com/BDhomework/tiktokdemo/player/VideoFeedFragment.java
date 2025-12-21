package com.BDhomework.tiktokdemo.player;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoFeedFragment extends Fragment {

    private static final String ARG_FEED_LIST = "arg_feed_list";
    private static final String ARG_START_POSITION = "arg_start_position";

    private ViewPager2 viewPager2;
    private VideoPagerAdapter adapter;
    private ArrayList<FeedItem> feedItems;
    private int startPosition = 0;

    private VideoPlayerManager playerManager;
    private VideoFeedViewModel viewModel;

    private int lastHandledIndex = -1;

    // ✅ 只让“进入页面的第一条首帧”触发一次 Activity 的 firstFrameCover 淡出
    private boolean firstFrameCoverHidden = false;

    public static VideoFeedFragment newInstance(List<FeedItem> feedItems, int startPosition) {
        VideoFeedFragment fragment = new VideoFeedFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED_LIST, new ArrayList<>(feedItems));
        args.putInt(ARG_START_POSITION, startPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerManager = VideoPlayerManager.getInstance(requireContext());
        viewPager2 = view.findViewById(R.id.video_pager);

        if (getArguments() != null) {
            //noinspection unchecked
            feedItems = (ArrayList<FeedItem>) getArguments().getSerializable(ARG_FEED_LIST);
            startPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }

        if (feedItems == null || feedItems.isEmpty()) return;

        viewModel = new ViewModelProvider(
                this,
                new VideoFeedViewModel.Factory(feedItems, startPosition)
        ).get(VideoFeedViewModel.class);

        adapter = new VideoPagerAdapter(this, new ArrayList<>(feedItems));
        viewPager2.setAdapter(adapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);
        viewPager2.setCurrentItem(startPosition, false);

        // Fragment 通过 observe “订阅” UiState
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);

        // 触发首个 index 的加载/播放
        viewPager2.post(() -> viewModel.onPageSelected(viewModel.getCurrentIndex()));
    }

    @Override
    public void onPause() {
        super.onPause();
        playerManager.pauseAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (viewPager2 != null) {
            viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
        }

        lastHandledIndex = -1;
        firstFrameCoverHidden = false;
    }

    private void renderState(@Nullable VideoFeedUiState state) {
        if (state == null) return;
        if (state.getItems() == null || state.getItems().isEmpty()) return;

        this.feedItems = new ArrayList<>(state.getItems());

        if (viewPager2 != null && viewPager2.getCurrentItem() != state.getCurrentIndex()) {
            viewPager2.setCurrentItem(state.getCurrentIndex(), false);
        }

        if (lastHandledIndex != state.getCurrentIndex()) {
            handlePageSelected(state.getCurrentIndex(), state.getPreloadRadius());
            lastHandledIndex = state.getCurrentIndex();
        }
    }

    private void handlePageSelected(int position, int preloadRadius) {
        if (feedItems == null || position < 0 || position >= feedItems.size()) return;

        // 只让当前页播放
        for (Integer bound : new HashSet<>(playerManager.getBoundPositions())) {
            if (bound != position) {
                ExoPlayer p = playerManager.getPlayerForPosition(bound);
                if (p != null) p.setPlayWhenReady(false);
            }
        }

        releaseFarPositions(position, preloadRadius);
        preparePosition(position, true, 0);
        preloadNeighbors(position, preloadRadius);
    }

    private void preparePosition(int position, boolean playWhenReady, int retry) {
        if (feedItems == null || position < 0 || position >= feedItems.size()) return;

        VideoPageFragment fragment = adapter.getFragmentAt(position);
        if (fragment == null || fragment.getPlayerView() == null) {
            // fragment 可能还没创建出来：轻量重试几次
            if (retry < 6 && viewPager2 != null) {
                viewPager2.postDelayed(
                        () -> preparePosition(position, playWhenReady, retry + 1),
                        50
                );
            }
            return;
        }

        String url = fragment.getVideoUrl();
        ExoPlayer player = playerManager.prepare(fragment.getPlayerView(), url, position, playWhenReady);
        if (player != null) {
            fragment.bindPlayer(player, url, playWhenReady);
        }
    }

    private void preloadNeighbors(int center, int radius) {
        if (feedItems == null) return;
        for (int i = 1; i <= radius; i++) {
            preloadNeighbor(center - i);
            preloadNeighbor(center + i);
        }
    }

    private void preloadNeighbor(int position) {
        if (position < 0 || feedItems == null || position >= feedItems.size()) return;
        preparePosition(position, false, 0);
    }

    private void releaseFarPositions(int centerPosition, int radius) {
        Set<Integer> keep = new HashSet<>();
        keep.add(centerPosition);
        for (int i = 1; i <= radius; i++) {
            keep.add(centerPosition - i);
            keep.add(centerPosition + i);
        }

        for (Integer bound : new HashSet<>(playerManager.getBoundPositions())) {
            if (!keep.contains(bound)) {
                VideoPageFragment fragment = adapter.getFragmentAt(bound);
                if (fragment != null) fragment.clearPlayer();
                playerManager.releasePosition(bound);
            }
        }
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (viewModel != null) {
                        viewModel.onPageSelected(position);
                    }
                }
            };

    /**
     * ✅ VideoPageFragment 首帧回调会走到这里：
     * - 只对“当前页”生效（避免预加载页提前触发）
     * - 只触发一次（进入页面首帧淡出 Activity 的 firstFrameCover）
     */
    public void onFirstFrameRenderedFromPage(int pos) {
        if (viewModel == null) return;
        if (pos != viewModel.getCurrentIndex()) return;
        if (firstFrameCoverHidden) return;

        firstFrameCoverHidden = true;

        Activity act = getActivity();
        if (act instanceof VideoFeedActivity) {
            act.runOnUiThread(() -> ((VideoFeedActivity) act).hideFirstFrameCoverWithFade());
        }
    }
}
