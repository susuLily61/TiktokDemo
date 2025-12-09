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
    private final Map<Integer, VideoPageFragment> fragments = new HashMap<>();

    public VideoPagerAdapter(@NonNull Fragment fragment, List<FeedItem> feedItems) {
        super(fragment);
        this.feedItems = feedItems;
        fragment.getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                if (f instanceof VideoPageFragment) {
                    fragments.remove(((VideoPageFragment) f).getPosition());
                }
            }
        }, false);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VideoPageFragment fragment = VideoPageFragment.newInstance(feedItems.get(position), position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    @Nullable
    public VideoPageFragment getFragmentAt(int position) {
        return fragments.get(position);
    }
}
