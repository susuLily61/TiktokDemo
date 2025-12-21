package com.BDhomework.tiktokdemo.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoPagerAdapter extends FragmentStateAdapter {

    private final List<FeedItem> feedItems;
    private final Fragment hostFragment;
    private final Map<Integer, VideoPageFragment> fragments = new HashMap<>();

    public VideoPagerAdapter(@NonNull Fragment hostFragment, @NonNull List<FeedItem> feedItems) {
        super(hostFragment);
        this.hostFragment = hostFragment;
        this.feedItems = feedItems;

        // 维护 position -> fragment 映射，避免泄漏
        hostFragment.getChildFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        if (f instanceof VideoPageFragment) {
                            fragments.remove(((VideoPageFragment) f).getPosition());
                        }
                    }
                },
                false
        );
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VideoPageFragment page = VideoPageFragment.newInstance(feedItems.get(position), position);

        // double insurance：确保 position 一致
        page.setPosition(position);

        // 首帧回调：由 page -> host(VideoFeedFragment) -> activity(VideoFeedActivity)
        page.setFirstFrameCallback(pos -> {
            if (!hostFragment.isAdded()) return;

            // 保守起见：确保在主线程执行
            hostFragment.requireActivity().runOnUiThread(() -> {
                if (hostFragment instanceof VideoFeedFragment) {
                    ((VideoFeedFragment) hostFragment).onFirstFrameRenderedFromPage(pos);
                }
            });
        });

        fragments.put(position, page);
        return page;
    }

    @Override
    public int getItemCount() {
        return feedItems == null ? 0 : feedItems.size();
    }

    @Nullable
    public VideoPageFragment getFragmentAt(int position) {
        return fragments.get(position);
    }
}
