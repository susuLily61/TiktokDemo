package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerManager = VideoPlayerManager.getInstance(requireContext());
        viewPager2 = view.findViewById(R.id.video_pager);

        if (getArguments() != null) {
            feedItems = (ArrayList<FeedItem>) getArguments().getSerializable(ARG_FEED_LIST);
            startPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }

        if (feedItems == null || feedItems.isEmpty()) {
            return;
        }

        adapter = new VideoPagerAdapter(this, feedItems);
        viewPager2.setAdapter(adapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);
        viewPager2.setCurrentItem(startPosition, false);
        viewPager2.post(() -> handlePageSelected(startPosition));
    }

    @Override
    public void onPause() {
        super.onPause();
        playerManager.pauseAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 临时先不要彻底释放播放器
        //playerManager.release();
    }

    private void handlePageSelected(int position) {
        if (feedItems == null || position < 0 || position >= feedItems.size()) {
            return;
        }
        releaseFarPositions(position);
        preparePosition(position, true);
        preloadNeighbor(position - 1);
        preloadNeighbor(position + 1);
    }

    private void preparePosition(int position, boolean playWhenReady) {
        VideoPageFragment fragment = adapter.getFragmentAt(position);
        if (fragment == null || fragment.getPlayerView() == null) {
            return;
        }
        String url = fragment.getVideoUrl();
        ExoPlayer player = playerManager.prepare(fragment.getPlayerView(), url, position, playWhenReady);
        if (player != null) {
            fragment.bindPlayer(player, url, playWhenReady);
        }
    }

    private void preloadNeighbor(int position) {
        if (position < 0 || feedItems == null || position >= feedItems.size()) {
            return;
        }
        preparePosition(position, false);
    }

    private void releaseFarPositions(int centerPosition) {
        Set<Integer> keep = new HashSet<>();
        keep.add(centerPosition);
        keep.add(centerPosition - 1);
        keep.add(centerPosition + 1);
        for (Integer bound : new HashSet<>(playerManager.getBoundPositions())) {
            if (!keep.contains(bound)) {
                VideoPageFragment fragment = adapter.getFragmentAt(bound);
                if (fragment != null) {
                    fragment.clearPlayer();
                }
                playerManager.releasePosition(bound);
            }
        }
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            handlePageSelected(position);
        }
    };
}
