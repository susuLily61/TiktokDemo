package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class VideoFeedActivity extends AppCompatActivity {

    public static final String EXTRA_FEED_LIST = "extra_feed_list";
    public static final String EXTRA_FEED_ID = "extra_feed_id";
    public static final String EXTRA_FEED_POSITION = "extra_feed_position";

    private ImageView transitionCover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);

        transitionCover = findViewById(R.id.video_transition_cover);

        ArrayList<FeedItem> feedItems =
                (ArrayList<FeedItem>) getIntent().getSerializableExtra(EXTRA_FEED_LIST);
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

        // 当前点击的视频 item，用于显示封面
        FeedItem currentItem = feedItems.get(startPosition);

        // 显示全屏过渡封面，并加载图片
        transitionCover.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(currentItem.getCoverUrl())
                .dontAnimate()
                .into(transitionCover);

        // ⚠ 这里不再用 postDelayed，具体“什么时候关”交给 Fragment/ExoPlayer 控制

        // 加载视频内流 Fragment
        if (savedInstanceState == null) {
            VideoFeedFragment fragment = VideoFeedFragment.newInstance(feedItems, startPosition);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.video_feed_container, fragment).commit();
        }
    }

    /**
     * 给 Fragment 调用：把顶部过渡封面淡出并隐藏
     */
    public void hideTransitionCoverWithFade() {
        if (transitionCover == null) return;
        if (transitionCover.getVisibility() != View.VISIBLE) return;

        transitionCover.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    transitionCover.setVisibility(View.GONE);
                    transitionCover.setAlpha(1f); // 还原，避免下次透明
                })
                .start();
    }

    public String debugTransitionCoverState() {
        if (transitionCover == null) return "null";
        return "vis=" + transitionCover.getVisibility()
                + ",alpha=" + transitionCover.getAlpha()
                + ",shown=" + transitionCover.isShown();
    }

}
