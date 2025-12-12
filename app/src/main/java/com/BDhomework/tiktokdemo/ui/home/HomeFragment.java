package com.BDhomework.tiktokdemo.ui.home;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.BDhomework.tiktokdemo.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TabLayout tabLayout = root.findViewById(R.id.top_tabs);
        ViewPager2 viewPager = root.findViewById(R.id.top_viewpager);

        TopTabAdapter adapter = new TopTabAdapter(this);
        viewPager.setAdapter(adapter);

        // 绑定 TabLayout 和 ViewPager2，创建自定义 Tab 文本
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            TextView tv = new TextView(getContext());
            tv.setText(adapter.getTabTitle(position));
            tv.setTextSize(13);
            tv.setGravity(Gravity.CENTER);
            tv.setSingleLine(true);
            tv.setIncludeFontPadding(false);
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setPadding(dp(8), 0, dp(8), 0);   // 控制 tab 间距

            Typeface type = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            tv.setTypeface(type, Typeface.NORMAL);

            tab.setCustomView(tv);
        }).attach();

        // 默认切到“推荐”（index = 1）
        viewPager.setCurrentItem(1, false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tv = (TextView) tab.getCustomView();
                if (tv != null) {
                    tv.setTypeface(
                            Typeface.create("sans-serif-medium", Typeface.BOLD)
                    );
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tv = (TextView) tab.getCustomView();
                if (tv != null) {
                    tv.setTypeface(
                            Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    );
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });


        tabLayout.post(() -> {
            ViewGroup slidingTabIndicator = (ViewGroup) tabLayout.getChildAt(0);
            for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
                View tabView = slidingTabIndicator.getChildAt(i);

                tabView.setMinimumWidth(0);
                tabView.setPadding(0, 0, 0, 0);

                ViewGroup.LayoutParams params = tabView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tabView.setLayoutParams(params);
            }

            // 让当前选中页（此时是“推荐”）加粗一次
            int selectedPos = tabLayout.getSelectedTabPosition();
            TabLayout.Tab selectedTab = tabLayout.getTabAt(selectedPos);
            if (selectedTab != null) {
                TextView tv = (TextView) selectedTab.getCustomView();
                if (tv != null) {
                    tv.setTypeface(
                            Typeface.create("sans-serif-medium", Typeface.BOLD)
                    );
                }
            }
        });

        return root;
    }

    // dp 转 px
    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}