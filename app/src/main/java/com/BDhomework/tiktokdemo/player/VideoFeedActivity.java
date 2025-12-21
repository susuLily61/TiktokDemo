package com.BDhomework.tiktokdemo.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.BDhomework.tiktokdemo.ai.DraggableFloatingBall;
import com.BDhomework.tiktokdemo.ai.AiChatActivity;

public class VideoFeedActivity extends AppCompatActivity {
    public static final String EXTRA_FEED_LIST = "extra_feed_list";
    public static final String EXTRA_FEED_ID = "extra_feed_id";
    public static final String EXTRA_FEED_POSITION = "extra_feed_position";

    private ImageView transitionCover;

    private volatile boolean enteringTransitionRunning = true;
    private volatile boolean firstFrameArrived = false;
    private volatile boolean coverDisposed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);
        DraggableFloatingBall ball = findViewById(R.id.ai_floating_ball_video);
        ball.setOnClickListener(v -> startActivity(new Intent(this, AiChatActivity.class)));

        transitionCover = findViewById(R.id.video_transition_cover);

        setupSharedElementGuard();

        ArrayList<FeedItem> feedItems =
                (ArrayList<FeedItem>) getIntent().getSerializableExtra(EXTRA_FEED_LIST);
        int startPosition = getIntent().getIntExtra(EXTRA_FEED_POSITION, 0);
        if (feedItems == null || feedItems.isEmpty()) { finish(); return; }

        FeedItem currentItem = feedItems.get(startPosition);
        Log.d("COVER_ST", "before scaleType=" + transitionCover.getScaleType());
        Glide.with(this).load(currentItem.getCoverUrl()).dontAnimate().into(transitionCover);
        transitionCover.post(() ->
                Log.d("COVER_ST", "after scaleType=" + transitionCover.getScaleType()
                        + ", drawable=" + transitionCover.getDrawable())
        );
        transitionCover.setVisibility(View.VISIBLE);
        transitionCover.setAlpha(1f);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.video_feed_container, VideoFeedFragment.newInstance(feedItems, startPosition))
                    .commit();
        }
    }

    /** Page 首帧渲染完成时调用：不渐隐，只做“条件满足则隐藏” */
    public void onFirstFrameRendered() {
        firstFrameArrived = true;
        tryHideTransitionCover();
    }

    private void tryHideTransitionCover() {
        if (coverDisposed) return;
        if (!firstFrameArrived) return;
        if (enteringTransitionRunning) return; // 转场没结束先别关，防复活/闪

        coverDisposed = true;

        if (transitionCover != null) {
            transitionCover.animate().cancel();
            transitionCover.setAlpha(1f);
            transitionCover.setVisibility(View.GONE);
            ViewCompat.setTransitionName(transitionCover, null);
        }
    }

    private void setupSharedElementGuard() {
        getWindow().setSharedElementsUseOverlay(false);

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> names, List<View> elements, List<View> snapshots) {
                enteringTransitionRunning = true;
            }

            @Override
            public void onSharedElementEnd(List<String> names, List<View> elements, List<View> snapshots) {
                enteringTransitionRunning = false;
                tryHideTransitionCover();
            }

            @Override
            public View onCreateSnapshotView(Context context, Parcelable snapshot) {
                View v = super.onCreateSnapshotView(context, snapshot);
                if (v instanceof ImageView) {
                    ((ImageView) v).setScaleType(ImageView.ScaleType.FIT_CENTER);
                    v.setBackgroundColor(Color.BLACK);
                }
                return v;
            }
        });

        Transition se = getWindow().getSharedElementEnterTransition();
        Transition enter = getWindow().getEnterTransition();

        Transition.TransitionListener listener = new Transition.TransitionListener() {
            @Override public void onTransitionStart(Transition transition) { enteringTransitionRunning = true; }
            @Override public void onTransitionEnd(Transition transition) { enteringTransitionRunning = false; tryHideTransitionCover(); transition.removeListener(this); }
            @Override public void onTransitionCancel(Transition transition) { enteringTransitionRunning = false; tryHideTransitionCover(); transition.removeListener(this); }
            @Override public void onTransitionPause(Transition transition) {}
            @Override public void onTransitionResume(Transition transition) {}
        };

        if (se != null) se.addListener(listener);
        if (enter != null) enter.addListener(listener);

        if (se == null && enter == null) enteringTransitionRunning = false;
    }
}
