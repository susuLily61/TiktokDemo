package com.BDhomework.tiktokdemo.player;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.List;

public class VideoPagerAdapter extends FragmentStateAdapter {

    private final List<FeedItem> feedItems;

    public VideoPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<FeedItem> feedItems) {
        super(fragmentActivity);
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return VideoPageFragment.newInstance(feedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }
}
