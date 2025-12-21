package com.BDhomework.tiktokdemo.player;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.SharedElementCallback;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoFeedActivity extends AppCompatActivity {

    public static final String EXTRA_FEED_LIST = "extra_feed_list";
    public static final String EXTRA_FEED_ID = "extra_feed_id";
    public static final String EXTRA_FEED_POSITION = "extra_feed_position";

    private ImageView transitionCover;     // shared element 专用
    private ImageView firstFrameCover;     // 业务层封面（首帧淡出）

    private volatile boolean enteringTransitionRunning = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 必须在 super 前
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);

        transitionCover = findViewById(R.id.video_transition_cover);
        firstFrameCover = findViewById(R.id.video_first_frame_cover);

        setupSharedElementGuard();

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
        if (startPosition < 0) startPosition = 0;

        FeedItem currentItem = feedItems.get(startPosition);

        // 两层都加载同一张封面
        Glide.with(this).load(currentItem.getCoverUrl()).dontAnimate().into(transitionCover);
        Glide.with(this).load(currentItem.getCoverUrl()).dontAnimate().into(firstFrameCover);

        // 初始状态
        transitionCover.setVisibility(View.VISIBLE);
        transitionCover.setAlpha(1f);

        firstFrameCover.setVisibility(View.VISIBLE);
        firstFrameCover.setAlpha(1f);

        // 加载 Fragment
        if (savedInstanceState == null) {
            VideoFeedFragment fragment =
                    VideoFeedFragment.newInstance(feedItems, startPosition);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.video_feed_container, fragment)
                    .commit();
        }
    }

    /**
     * shared element 的“防复活护栏”
     */
    private void setupSharedElementGuard() {
        getWindow().setSharedElementsUseOverlay(false);

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> names,
                                             List<View> elements,
                                             List<View> snapshots) {
                enteringTransitionRunning = true;
            }

            @Override
            public void onSharedElementEnd(List<String> names,
                                           List<View> elements,
                                           List<View> snapshots) {
                enteringTransitionRunning = false;
                hideSharedElementCoverForever();
            }
        });

        Transition se = getWindow().getSharedElementEnterTransition();
        Transition enter = getWindow().getEnterTransition();

        Transition.TransitionListener listener = new Transition.TransitionListener() {
            @Override public void onTransitionStart(Transition transition) {
                enteringTransitionRunning = true;
            }

            @Override public void onTransitionEnd(Transition transition) {
                enteringTransitionRunning = false;
                hideSharedElementCoverForever();
                transition.removeListener(this);
            }

            @Override public void onTransitionCancel(Transition transition) {
                enteringTransitionRunning = false;
                hideSharedElementCoverForever();
                transition.removeListener(this);
            }

            @Override public void onTransitionPause(Transition transition) {}
            @Override public void onTransitionResume(Transition transition) {}
        };

        if (se != null) se.addListener(listener);
        if (enter != null) enter.addListener(listener);

        if (se == null && enter == null) {
            enteringTransitionRunning = false;
        }
    }

    /**
     * shared element 用完即弃
     */
    private void hideSharedElementCoverForever() {
        transitionCover.animate().cancel();
        transitionCover.setAlpha(0f);
        transitionCover.setVisibility(View.GONE);
        ViewCompat.setTransitionName(transitionCover, null);

        transitionCover.post(() -> {
            transitionCover.setAlpha(0f);
            transitionCover.setVisibility(View.GONE);
            ViewCompat.setTransitionName(transitionCover, null);
        });
    }

    /**
     * Fragment 在“首帧渲染完成”时调用
     */
    public void hideFirstFrameCoverWithFade() {
        if (firstFrameCover == null) return;
        if (firstFrameCover.getVisibility() == View.GONE) return;

        firstFrameCover.animate().cancel();
        firstFrameCover.setVisibility(View.VISIBLE);
        firstFrameCover.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    firstFrameCover.setVisibility(View.GONE);
                    firstFrameCover.setAlpha(1f);
                })
                .start();
    }
}
