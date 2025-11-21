package com.BDhomework.tiktokdemo.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.BDhomework.tiktokdemo.ui.common.PlaceholderFragment;
import com.BDhomework.tiktokdemo.ui.recommend.RecommendFragment;

public class TopTabAdapter extends FragmentStateAdapter {

    private static final String[] TABS = new String[]{"关注", "推荐", "同城", "商城"};

    public TopTabAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return RecommendFragment.newInstance();
        }
        return PlaceholderFragment.newInstance(TABS[position]);
    }

    @Override
    public int getItemCount() {
        return TABS.length;
    }

    public String getTabTitle(int position) {
        return TABS[position];
    }
}
