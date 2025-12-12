package com.BDhomework.tiktokdemo.player;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.Spanned;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.comment.CommentBottomSheetDialog;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;

import java.util.Random;

public class VideoPageFragment extends Fragment {

    private static final String ARG_FEED = "arg_feed";
    private static final String ARG_POSITION = "arg_position";

    private FeedItem feedItem;
    private String videoUrl;
    private PlayerView playerView;
    private TextView likeCountView;
    private TextView commentCountView;
    private TextView collectCountView;
    private TextView shareCountView;
    private ImageView likeButton;
    private ImageView collectButton;
    private ImageView avatarView;
    private TextView descriptionView;
    private TimeBar timeBar;
    private boolean liked = false;
    private boolean collected = false;
    private int baseCommentCount;
    private int baseCollectCount;
    private int baseShareCount;
    private boolean isScrubbing = false;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private GestureDetectorCompat gestureDetector;
    private boolean playerListenerAttached = false;
    private ExoPlayer currentPlayer;
    private Player.Listener playerListener;
    private int position;

    private ImageView coverView;

    private View musicDiscView;
    private ObjectAnimator musicDiscAnimator;

    private ImageView playIndicator;          // 中间播放键
    private FrameLayout heartAnimLayer;       // 双击爱心动画容器

    private boolean wasPlayingBeforeScrub = false;


    public static VideoPageFragment newInstance(FeedItem item, int position) {
        VideoPageFragment fragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED, item);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getPosition() {
        return position;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_page, container, false);
        feedItem = (FeedItem) getArguments().getSerializable(ARG_FEED);
        position = getArguments().getInt(ARG_POSITION, -1);
        if (feedItem != null) {
            videoUrl = feedItem.getVideoUrl();
        }

        playerView = root.findViewById(R.id.video_player_view);
        coverView = root.findViewById(R.id.video_cover);

        TextView author = root.findViewById(R.id.video_author);
        descriptionView = root.findViewById(R.id.video_description);
        avatarView = root.findViewById(R.id.video_avatar);
        likeCountView = root.findViewById(R.id.video_like_count);
        commentCountView = root.findViewById(R.id.video_comment_count);
        collectCountView = root.findViewById(R.id.video_collect_count);
        shareCountView = root.findViewById(R.id.video_share_count);
        ImageView commentButton = root.findViewById(R.id.video_comment_button);
        ImageView shareButton = root.findViewById(R.id.video_share_button);
        likeButton = root.findViewById(R.id.video_like_button);
        collectButton = root.findViewById(R.id.video_collect_button);
        ImageView backButton = root.findViewById(R.id.video_back_button);
        LinearLayout likeContainer = root.findViewById(R.id.action_like);
        LinearLayout commentContainer = root.findViewById(R.id.action_comment);
        LinearLayout collectContainer = root.findViewById(R.id.action_collect);
        LinearLayout shareContainer = root.findViewById(R.id.action_share);
        View commentEntryBar = root.findViewById(R.id.comment_entry_bar);
        View commentEntryText = root.findViewById(R.id.comment_entry_text);
        View commentEntryImage = root.findViewById(R.id.comment_entry_image);
        View commentEntryMention = root.findViewById(R.id.comment_entry_mention);
        View commentEntryEmoji = root.findViewById(R.id.comment_entry_emoji);
        timeBar = root.findViewById(R.id.video_time_bar);

        musicDiscView = root.findViewById(R.id.video_music_disc);
        setupMusicDiscRotation();

        playIndicator = root.findViewById(R.id.video_pause_indicator);
        heartAnimLayer = root.findViewById(R.id.heart_anim_layer);

        if (feedItem != null) {
            author.setText("@" + feedItem.getAuthorName());
            likeCountView.setText(String.valueOf(feedItem.getLikeCount()));
            baseCommentCount = 80 + new Random().nextInt(140);
            baseCollectCount = 50 + new Random().nextInt(120);
            baseShareCount = new Random().nextInt(200);
            commentCountView.setText(String.valueOf(baseCommentCount));
            collectCountView.setText(String.valueOf(baseCollectCount));
            shareCountView.setText(String.valueOf(baseShareCount));
            bindAvatar(feedItem.getAvatarUrl());
            setupDescription(feedItem.getTitle(), feedItem.getDescription());

            if (coverView != null && feedItem.getCoverUrl() != null) {
                Glide.with(this)
                        .load(feedItem.getCoverUrl())
                        .centerCrop()
                        .placeholder(R.drawable.video_placeholder) // 没有就先随便放一个占位图资源
                        .into(coverView);
            }
        }


        gestureDetector = new GestureDetectorCompat(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true; // 必须返回 true 才能继续收到后续事件（包括双击）
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        handleDoubleTap(e);
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        togglePlayPause(); // 不再传参，用成员 playIndicator
                        return true;
                    }
                });

        playerView.setClickable(true);

        playerView.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);

            // handled == true  → 单击 / 双击 → 我们自己吃掉
            // handled == false → 滑动 → 交给 ViewPager2 处理上下滑
            return handled;
        });


        View.OnClickListener commentClick = v -> showComments();
        commentButton.setOnClickListener(commentClick);
        commentContainer.setOnClickListener(commentClick);
        commentEntryBar.setOnClickListener(commentClick);
        commentEntryText.setOnClickListener(commentClick);
        commentEntryImage.setOnClickListener(commentClick);
        commentEntryMention.setOnClickListener(commentClick);
        commentEntryEmoji.setOnClickListener(commentClick);

        shareButton.setOnClickListener(v -> Toast.makeText(requireContext(), "分享功能待接入", Toast.LENGTH_SHORT).show());
        shareContainer.setOnClickListener(v -> Toast.makeText(requireContext(), "分享功能待接入", Toast.LENGTH_SHORT).show());
        likeButton.setOnClickListener(v -> toggleLikeByButton());
        likeContainer.setOnClickListener(v -> toggleLikeByButton());
        collectButton.setOnClickListener(v -> toggleCollect());
        collectContainer.setOnClickListener(v -> toggleCollect());
        backButton.setOnClickListener(v -> requireActivity().finish());

        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                isScrubbing = true;
                if (currentPlayer != null) {
                    wasPlayingBeforeScrub = currentPlayer.getPlayWhenReady();
                    currentPlayer.setPlayWhenReady(false);
                }
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                // no-op
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                isScrubbing = false;
                if (currentPlayer != null) {
                    currentPlayer.seekTo(position);
                    currentPlayer.setPlayWhenReady(wasPlayingBeforeScrub);
                    // 同步中间播放键
                    if (playIndicator != null) {
                        playIndicator.setVisibility(wasPlayingBeforeScrub ? View.GONE : View.VISIBLE);
                        playIndicator.setAlpha(0.65f);
                    }
                }
                scheduleProgressUpdate();
            }
        });

        return root;
    }

    //双击爱心动画
    private void showDoubleTapHeart(float x, float y) {
        if (heartAnimLayer == null || playerView == null) return;

        ImageView heart = new ImageView(requireContext());
        int size = (int) (120 * getResources().getDisplayMetrics().density);
        heart.setImageResource(R.drawable.ic_heart_big_filled);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        heart.setLayoutParams(lp);

        int[] pvLoc = new int[2];
        int[] layerLoc = new int[2];
        playerView.getLocationOnScreen(pvLoc);
        heartAnimLayer.getLocationOnScreen(layerLoc);

        float layerX = (pvLoc[0] + x) - layerLoc[0];
        float layerY = (pvLoc[1] + y) - layerLoc[1];

        heart.setX(layerX - size / 2f);
        heart.setY(layerY - size / 2f);

        heart.setScaleX(0.2f);
        heart.setScaleY(0.2f);
        heart.setAlpha(0f);

        heartAnimLayer.addView(heart);

        float dy = 120 * getResources().getDisplayMetrics().density;

        heart.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(160)
                .withEndAction(() -> heart.animate()
                        .translationYBy(-dy)
                        .alpha(0f)
                        .setDuration(320)
                        .withEndAction(() -> heartAnimLayer.removeView(heart))
                        .start())
                .start();
    }

    private void handleDoubleTap(MotionEvent e) {
        Log.d("Gesture", "double tap x=" + e.getX() + ", y=" + e.getY());
        // 每次双击都要有爱心动画
        showDoubleTapHeart(e.getX(), e.getY());

        // 只有第一次双击才真正点赞（+1 + 变红）
        if (!liked) {
            setLiked(true, true);
        }
    }

    private void bindAvatar(String avatarUrl) {
        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_circle_placeholder)
                .circleCrop()
                .into(avatarView);
    }

    private void setupDescription(String title, String description) {
        String fullText = title;
        if (!TextUtils.isEmpty(description)) {
            fullText = title + " " + description;
        }
        final String finalFullText = fullText;
        descriptionView.setHighlightColor(0x00000000);
        descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        descriptionView.post(() -> applyCollapsedText(finalFullText, true));
    }

    private void applyCollapsedText(String fullText, boolean collapsed) {
        if (!collapsed) {
            SpannableStringBuilder builder = new SpannableStringBuilder(fullText + " 收起");
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    applyCollapsedText(fullText, true);
                }
            }, fullText.length() + 1, fullText.length() + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            descriptionView.setMaxLines(Integer.MAX_VALUE);
            descriptionView.setEllipsize(null);
            descriptionView.setText(builder);
            return;
        }

        descriptionView.setMaxLines(2);
        descriptionView.setEllipsize(TextUtils.TruncateAt.END);
        descriptionView.setText(fullText);

        descriptionView.post(() -> {
            if (descriptionView.getLayout() != null && descriptionView.getLayout().getLineCount() > 2) {
                int end = descriptionView.getLayout().getLineEnd(1);
                String truncated = fullText.substring(0, Math.min(end, fullText.length())).trim();
                String display = truncated + " …展开";
                SpannableStringBuilder builder = new SpannableStringBuilder(display);
                int start = display.indexOf("展开");
                if (start >= 0) {
                    builder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            applyCollapsedText(fullText, false);
                        }
                    }, start, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                descriptionView.setText(builder);
            }
        });
    }

    private void setLiked(boolean newLiked, boolean updateCount) {
        liked = newLiked;

        int baseCount = feedItem != null ? feedItem.getLikeCount() : 0;
        if (updateCount) {
            int displayCount = liked ? baseCount + 1 : baseCount;
            likeCountView.setText(String.valueOf(displayCount));
        }

        likeButton.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void toggleLikeByButton() {
        // 按钮点击允许取消/恢复，并更新计数
        setLiked(!liked, true);
    }


    private void toggleCollect() {
        collected = !collected;
        int displayCount = collected ? baseCollectCount + 1 : baseCollectCount;
        collectCountView.setText(String.valueOf(displayCount));
        collectButton.setImageResource(collected ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    //音乐转盘旋转动画
    private void setupMusicDiscRotation() {
        if (musicDiscView == null) return;

        musicDiscAnimator = ObjectAnimator.ofFloat(musicDiscView, View.ROTATION, 0f, 360f);
        musicDiscAnimator.setDuration(6000L); // 6秒一圈
        musicDiscAnimator.setInterpolator(new LinearInterpolator());
        musicDiscAnimator.setRepeatCount(ValueAnimator.INFINITE);
        musicDiscAnimator.setRepeatMode(ValueAnimator.RESTART);
        // 关键：先 start 一下再 pause，保证后续 resume 生效
        musicDiscAnimator.start();
        musicDiscAnimator.pause();
    }

    private void setMusicDiscSpinning(boolean spinning) {
        if (musicDiscAnimator == null) return;
        if (spinning) {
            if (!musicDiscAnimator.isStarted()) musicDiscAnimator.start();
            else musicDiscAnimator.resume();
        } else {
            musicDiscAnimator.pause();
        }
    }

    private void togglePlayPause() {
        if (currentPlayer == null || playIndicator == null) return;

        boolean wasPlaying = currentPlayer.getPlayWhenReady();
        boolean nowPlaying = !wasPlaying;
        currentPlayer.setPlayWhenReady(nowPlaying);

        if (nowPlaying) {
            // 播放：隐藏中间播放键
            playIndicator.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                playIndicator.setVisibility(View.GONE);
                playIndicator.setAlpha(0.65f);
            }).start();
        } else {
            // 暂停：显示中间播放键
            playIndicator.setAlpha(0f);
            playIndicator.setVisibility(View.VISIBLE);
            playIndicator.animate().alpha(0.65f).setDuration(120).start();
        }

        setMusicDiscSpinning(nowPlaying);
    }


    private void showComments() {
        CommentBottomSheetDialog dialog = new CommentBottomSheetDialog();
        dialog.setOnCommentAddedListener(totalCount -> commentCountView.setText(String.valueOf(Math.max(totalCount, baseCommentCount))));
        dialog.show(getChildFragmentManager(), "comments");
    }

    public void bindPlayer(@NonNull ExoPlayer player, @NonNull String url, boolean playWhenReady) {
        Log.d("VP", "bindPlayer pos=" + position + " playWhenReady? " + player.getPlayWhenReady() + " url=" + url);
        if (currentPlayer != null && currentPlayer != player && playerListenerAttached) {
            currentPlayer.removeListener(playerListener);
            playerListenerAttached = false;
        }
        currentPlayer = player;
        videoUrl = url;
        playerView.setPlayer(player);
        attachPlayerListener();
        scheduleProgressUpdate();

        if (playIndicator != null) {
            if (playWhenReady) {
                playIndicator.setVisibility(View.GONE);
            } else {
                playIndicator.setAlpha(0.65f);
                playIndicator.setVisibility(View.VISIBLE);
            }
        }
        setMusicDiscSpinning(playWhenReady);

    }

    public void clearPlayer() {
        if (currentPlayer != null && playerListenerAttached) {
            currentPlayer.removeListener(playerListener);
        }
        playerListenerAttached = false;
        currentPlayer = null;
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        progressHandler.removeCallbacksAndMessages(null);
        updateTimeBar();

        setMusicDiscSpinning(false);

        if (playIndicator != null) {
            playIndicator.setAlpha(0.65f);
            playIndicator.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearPlayer();

        if (musicDiscAnimator != null) {
            musicDiscAnimator.cancel();
            musicDiscAnimator = null;
        }
        musicDiscView = null;

        playerView = null;
    }


    private void attachPlayerListener() {
        if (playerListenerAttached || currentPlayer == null) {
            return;
        }
        if (playerListener == null) {
            playerListener = new Player.Listener() {
                @Override
                public void onTimelineChanged(@NonNull com.google.android.exoplayer2.Timeline timeline, int reason) {
                    updateTimeBar();
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Log.d("VP", "state=" + playbackState + " playWhenReady=" + (currentPlayer != null && currentPlayer.getPlayWhenReady()));

                    updateTimeBar();

                    if (playbackState == Player.STATE_READY) {
                        // 缓冲好：隐藏封面
                        if (coverView != null) {
                            coverView.setVisibility(View.GONE);
                        }
                        // READY 时按 playWhenReady 决定是否转
                        setMusicDiscSpinning(currentPlayer != null && currentPlayer.getPlayWhenReady());
                    } else if (playbackState == Player.STATE_ENDED) {
                        // 播放结束：停转
                        setMusicDiscSpinning(false);
                    }
                }
            };
        }
        currentPlayer.addListener(playerListener);
        playerListenerAttached = true;
    }

    private void scheduleProgressUpdate() {
        progressHandler.removeCallbacksAndMessages(null);
        progressHandler.post(progressRunnable);
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeBar();
            if (isResumed() && !isScrubbing && currentPlayer != null) {
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    private void updateTimeBar() {
        if (timeBar == null) return;
        if (currentPlayer == null) {
            timeBar.setEnabled(false);
            return;
        }

        long duration = currentPlayer.getDuration();
        long position = currentPlayer.getCurrentPosition();
        long buffered = currentPlayer.getBufferedPosition();
        if (duration == C.TIME_UNSET || duration <= 0) {
            timeBar.setEnabled(false);
            return;
        }
        timeBar.setEnabled(true);
        timeBar.setDuration(duration);
        timeBar.setPosition(position);
        timeBar.setBufferedPosition(buffered);
    }

}
