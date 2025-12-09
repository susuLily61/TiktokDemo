package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class VideoFeedActivity extends AppCompatActivity {

    public static final String EXTRA_FEED_LIST = "extra_feed_list";
    public static final String EXTRA_FEED_ID = "extra_feed_id";
    public static final String EXTRA_FEED_POSITION = "extra_feed_position";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);
        ArrayList<FeedItem> feedItems = (ArrayList<FeedItem>) getIntent().getSerializableExtra(EXTRA_FEED_LIST);
        String selectedId = getIntent().getStringExtra(EXTRA_FEED_ID);
        int startPosition = getIntent().getIntExtra(EXTRA_FEED_POSITION, -1);

        if (feedItems == null) {
            finish();
            return;
        }

        if (startPosition < 0 && selectedId != null) {
            for (int i = 0; i < feedItems.size(); i++) {
                if (feedItems.get(i).getId().equals(selectedId)) {
                    startPosition = i;
                    break;
                }
            }
        }
        if (startPosition < 0) {
            startPosition = 0;
        }

        if (savedInstanceState == null) {
            VideoFeedFragment fragment = VideoFeedFragment.newInstance(feedItems, startPosition);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.video_feed_container, fragment).commit();
        }
    }
}
