package com.BDhomework.tiktokdemo.ui.home;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TabLayout tabLayout = root.findViewById(R.id.top_tabs);
        ViewPager2 viewPager = root.findViewById(R.id.top_viewpager);

        TopTabAdapter adapter = new TopTabAdapter(this);
        viewPager.setAdapter(adapter);

        // ★ 使用自定义 TextView 作为每个 Tab 的 UI
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            TextView tv = new TextView(getContext());
            tv.setText(adapter.getTabTitle(position));
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);

            // ★ 新增：给文字左右各 16dp 的内边距，这样 Tab 之间就不会挤在一起了
            int padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    getResources().getDisplayMetrics()
            );
            tv.setPadding(padding, 0, padding, 0);

            // 默认高亮“推荐”
            if (position == 1) {
                tv.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                tv.setTypeface(Typeface.DEFAULT);
            }

            tab.setCustomView(tv);
        }).attach();


        // ★ 添加 Tab 选中监听，选中时加粗，不选中时恢复
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tv = (TextView) tab.getCustomView();
                if (tv != null) {
                    tv.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tv = (TextView) tab.getCustomView();
                if (tv != null) {
                    tv.setTypeface(Typeface.DEFAULT);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return root;
    }
}

