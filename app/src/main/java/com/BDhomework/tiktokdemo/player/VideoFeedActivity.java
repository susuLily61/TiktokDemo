package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class VideoFeedActivity extends AppCompatActivity {

    public static final String EXTRA_FEED_LIST = "extra_feed_list";
    public static final String EXTRA_FEED_ID = "extra_feed_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);

        ViewPager2 viewPager2 = findViewById(R.id.video_pager);
        ArrayList<FeedItem> feedItems = (ArrayList<FeedItem>) getIntent().getSerializableExtra(EXTRA_FEED_LIST);
        String selectedId = getIntent().getStringExtra(EXTRA_FEED_ID);

        if (feedItems == null) {
            finish();
            return;
        }

        VideoPagerAdapter adapter = new VideoPagerAdapter(this, feedItems);
        viewPager2.setAdapter(adapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        int startPosition = 0;
        for (int i = 0; i < feedItems.size(); i++) {
            if (feedItems.get(i).getId().equals(selectedId)) {
                startPosition = i;
                break;
            }
        }
        viewPager2.setCurrentItem(startPosition, false);
    }
}
