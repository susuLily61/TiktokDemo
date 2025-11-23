package com.BDhomework.tiktokdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.BDhomework.tiktokdemo.ui.common.PlaceholderFragment;
import com.BDhomework.tiktokdemo.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private TextView navHome;
    private TextView navFriends;
    private TextView navMessage;
    private TextView navProfile;
    private ImageView navAdd;

    private enum Tab {
        HOME, FRIENDS, MESSAGE, PROFILE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 绑定底部栏控件
        navHome = findViewById(R.id.nav_home);
        navFriends = findViewById(R.id.nav_friends);
        navMessage = findViewById(R.id.nav_message);
        navProfile = findViewById(R.id.nav_profile);
        navAdd = findViewById(R.id.nav_add);

        // 2. 给 4 个文字按钮绑定点击，切换 Fragment
        View.OnClickListener tabClickListener = v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                selectTab(Tab.HOME);
                switchContent(HomeFragment.newInstance());
            } else if (id == R.id.nav_friends) {
                selectTab(Tab.FRIENDS);
                switchContent(PlaceholderFragment.newInstance("朋友"));
            } else if (id == R.id.nav_message) {
                selectTab(Tab.MESSAGE);
                switchContent(PlaceholderFragment.newInstance("消息"));
            } else if (id == R.id.nav_profile) {
                selectTab(Tab.PROFILE);
                switchContent(PlaceholderFragment.newInstance("我"));
            }
        };

        navHome.setOnClickListener(tabClickListener);
        navFriends.setOnClickListener(tabClickListener);
        navMessage.setOnClickListener(tabClickListener);
        navProfile.setOnClickListener(tabClickListener);

        // 3. 中间“发布”按钮：现在先用占位 Fragment，你之后可以改成真正的发布逻辑
        navAdd.setOnClickListener(v -> {
            // 发布按钮一般不算一个“选中的 Tab”，这里简单切到占位页，也可以什么都不切
            switchContent(PlaceholderFragment.newInstance("发布"));
            // 如果不想改变底部文字高亮，可以不调用 selectTab()
        });

        // 4. 首次进入默认选中“首页”
        if (savedInstanceState == null) {
            selectTab(Tab.HOME);
            switchContent(HomeFragment.newInstance());
        }
    }

    /**
     * 切换中间内容 Fragment
     */
    private void switchContent(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_container, fragment);
        transaction.commit();
    }

    /**
     * 处理底部栏文字的选中 / 未选中状态（高亮）
     */
    private void selectTab(Tab tab) {
        int selected = Color.parseColor("#000000");   // 选中：黑色
        int unselected = Color.parseColor("#777777"); // 未选中：灰色

        navHome.setTextColor(unselected);
        navFriends.setTextColor(unselected);
        navMessage.setTextColor(unselected);
        navProfile.setTextColor(unselected);

        if (tab == Tab.HOME) {
            navHome.setTextColor(selected);
        } else if (tab == Tab.FRIENDS) {
            navFriends.setTextColor(selected);
        } else if (tab == Tab.MESSAGE) {
            navMessage.setTextColor(selected);
        } else if (tab == Tab.PROFILE) {
            navProfile.setTextColor(selected);
        }
    }
}
