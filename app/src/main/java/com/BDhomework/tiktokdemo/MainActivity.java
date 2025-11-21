package com.BDhomework.tiktokdemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.BDhomework.tiktokdemo.ui.home.HomeFragment;
import com.BDhomework.tiktokdemo.ui.common.PlaceholderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                switchContent(HomeFragment.newInstance());
                return true;
            } else if (id == R.id.menu_friends) {
                switchContent(PlaceholderFragment.newInstance("朋友"));
                return true;
            } else if (id == R.id.menu_add) {
                switchContent(PlaceholderFragment.newInstance("发布"));
                return true;
            } else if (id == R.id.menu_message) {
                switchContent(PlaceholderFragment.newInstance("消息"));
                return true;
            } else if (id == R.id.menu_profile) {
                switchContent(PlaceholderFragment.newInstance("我"));
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.menu_home);
        }
    }

    private void switchContent(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_container, fragment);
        transaction.commit();
    }
}
