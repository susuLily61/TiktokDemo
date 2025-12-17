package com.BDhomework.tiktokdemo.player;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
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
import androidx.lifecycle.ViewModelProvider;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.comment.CommentBottomSheetDialog;
import com.BDhomework.tiktokdemo.comment.CommentViewModelFactory;
import com.BDhomework.tiktokdemo.data.repository.impl.MockFeedRepository;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;


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

    private boolean isScrubbing = false;
    private boolean wasPlayingBeforeScrub = false;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private GestureDetectorCompat gestureDetector;

    private ExoPlayer currentPlayer;
    private Player.Listener playerListener;
    private boolean playerListenerAttached = false;

    private int position;
    private VideoFeedViewModel viewModel;

    private ImageView coverView;

    private View musicDiscView;
    private ObjectAnimator musicDiscAnimator;

    private ImageView playIndicator;      // 中间播放键
    private FrameLayout heartAnimLayer;   // 双击爱心动画容器

    // ✅ 关键：第一帧兜底标记，确保“隐藏封面”只执行一次，且在每次 bind 时重置
    private boolean firstFrameRendered = false;

    public static VideoPageFragment newInstance(FeedItem item, int position) {
        VideoPageFragment fragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED, item);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public String getVideoUrl() { return videoUrl; }
    public int getPosition() { return position; }
    public PlayerView getPlayerView() { return playerView; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_page, container, false);

        viewModel = new ViewModelProvider(requireParentFragment()).get(VideoFeedViewModel.class);

        feedItem = (FeedItem) getArguments().getSerializable(ARG_FEED);
        position = getArguments().getInt(ARG_POSITION, -1);
        if (feedItem != null) videoUrl = feedItem.getVideoUrl();

        playerView = root.findViewById(R.id.video_player_view);
        playerView.setBackgroundColor(Color.TRANSPARENT);
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

        // ✅ 关键：创建页时先让封面可见（避免复用/复进时状态脏）
        if (coverView != null) {
            coverView.setVisibility(View.VISIBLE);
            coverView.setAlpha(1f);
        }

        if (feedItem != null) {
            author.setText("@" + feedItem.getAuthorName());
            bindAvatar(feedItem.getAvatarUrl());
            setupDescription(feedItem.getTitle(), feedItem.getDescription());

            if (coverView != null && feedItem.getCoverUrl() != null) {
                Glide.with(this)
                        .load(feedItem.getCoverUrl())
                        .centerCrop()
                        .placeholder(R.drawable.video_placeholder)
                        .into(coverView);
            }
        }

        // 手势
        gestureDetector = new GestureDetectorCompat(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true; // 必须 true
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        handleDoubleTap(e);
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        togglePlayPause();
                        return true;
                    }
                });

        playerView.setClickable(true);

        // ✅ 更稳：只在 ACTION_UP 时消费 handled，避免影响 ViewPager2 滑动/选中
        playerView.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                return handled;
            }
            return false;
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

        // 进度条拖拽
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                isScrubbing = true;
                if (currentPlayer != null) {
                    wasPlayingBeforeScrub = currentPlayer.getPlayWhenReady();
                    currentPlayer.setPlayWhenReady(false);
                }
            }

            @Override public void onScrubMove(TimeBar timeBar, long position) { }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                isScrubbing = false;
                if (currentPlayer != null) {
                    currentPlayer.seekTo(position);
                    currentPlayer.setPlayWhenReady(wasPlayingBeforeScrub);
                    if (playIndicator != null) {
                        playIndicator.setVisibility(wasPlayingBeforeScrub ? View.GONE : View.VISIBLE);
                        playIndicator.setAlpha(0.65f);
                    }
                    setMusicDiscSpinning(wasPlayingBeforeScrub);
                }
                scheduleProgressUpdate();
            }
        });

        if (viewModel != null) {
            viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderInteractionState);
            renderInteractionState(viewModel.getUiState().getValue());
        }

        logCover("onCreateView end");
        return root;
    }

    // 绑定播放器：每次都重置 firstFrame + 先显示封面，然后等待第一帧再隐藏
    public void bindPlayer(@NonNull ExoPlayer player, @NonNull String url, boolean playWhenReady) {
        // ① 防止重复绑定同一个 player
        if (currentPlayer == player
                && TextUtils.equals(videoUrl, url)
                && playerListenerAttached) {
            return;
        }

        // ② 如果是切换 player，先解旧 listener（不 return）
        if (currentPlayer != null && currentPlayer != player && playerListenerAttached) {
            currentPlayer.removeListener(playerListener);
            playerListenerAttached = false;
        }

        currentPlayer = player;
        videoUrl = url;

        firstFrameRendered = false;
        if (coverView != null) {
            coverView.setAlpha(1f);
            coverView.setVisibility(View.VISIBLE);
        }
        logCover("bindPlayer after show cover");
        playerView.setPlayer(player);
        attachPlayerListener();

        player.setPlayWhenReady(playWhenReady);
        // ✅ 这里补上 prepare，避免某些路径下没 prepare 导致时序怪
        player.prepare();
        logPlayerViewLayers("after prepare");
        logCover("bindPlayer after prepare");
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

        // ✅ 超级兜底：800ms 后如果在播但封面还在，就强制关（解决极少数不回调 firstFrame）
        playerView.postDelayed(() -> {
            if (!isAdded()) return;
            if (currentPlayer != null && currentPlayer.getPlayWhenReady()) {
                if (coverView != null && coverView.getVisibility() == View.VISIBLE) {
                    coverView.setVisibility(View.GONE);
                    if (getActivity() instanceof VideoFeedActivity) {
                        ((VideoFeedActivity) getActivity()).hideTransitionCoverWithFade();
                    }
                }
            }
        }, 800);
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

        logCover("clearPlayer");
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
        if (playerListenerAttached || currentPlayer == null) return;

        if (playerListener == null) {
            playerListener = new Player.Listener() {

                @Override
                public void onRenderedFirstFrame() {
                    if (firstFrameRendered) return;
                    firstFrameRendered = true;
                    logCover("onRenderedFirstFrame BEFORE");
                    logPlayerViewLayers("firstFrame BEFORE");
                    if (coverView != null) coverView.setVisibility(View.GONE);

                    // ✅ 兜底隐藏 Activity 的转场封面（解决你“有声音但封面不消失”的偶发）
                    if (getActivity() instanceof VideoFeedActivity) {
                        ((VideoFeedActivity) getActivity()).hideTransitionCoverWithFade();
                    }
                    logCover("onRenderedFirstFrame AFTER");
                    logPlayerViewLayers("firstFrame AFTER");
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    logCover("onPlaybackStateChanged=" + playbackState);
                    updateTimeBar();
                    if (playbackState == Player.STATE_ENDED) {
                        setMusicDiscSpinning(false);
                    }
                }
            };
        }

        currentPlayer.addListener(playerListener);
        playerListenerAttached = true;
    }

    // -------- 点赞 / 收藏 --------

    private void handleDoubleTap(MotionEvent e) {
        showDoubleTapHeart(e.getX(), e.getY());
        if (viewModel != null && feedItem != null && !viewModel.isLiked(feedItem.getId())) {
            viewModel.setLiked(feedItem.getId(), true);
        }
    }

    private void toggleLikeByButton() {
        if (viewModel != null && feedItem != null) {
            viewModel.toggleLike(feedItem.getId());
        }
    }

    private void toggleCollect() {
        if (viewModel != null && feedItem != null) {
            viewModel.toggleCollect(feedItem.getId());
        }
    }

    private void renderInteractionState(@Nullable VideoFeedUiState state) {
        if (state == null || feedItem == null) return;

        VideoFeedUiState.FeedInteractionState interaction = state.getFeedState(feedItem.getId());
        boolean liked = interaction != null && interaction.isLiked();
        boolean collected = interaction != null && interaction.isCollected();

        int likeCount = (interaction != null)
                ? interaction.getBaseLikeCount() + (liked ? 1 : 0)
                : feedItem.getLikeCount();
        int commentCount = (interaction != null)
                ? interaction.getBaseCommentCount()
                : safeParseInt(commentCountView.getText().toString());
        int collectCount = (interaction != null)
                ? interaction.getBaseCollectCount() + (collected ? 1 : 0)
                : safeParseInt(collectCountView.getText().toString());
        int shareCount = (interaction != null)
                ? interaction.getBaseShareCount()
                : safeParseInt(shareCountView.getText().toString());

        likeCountView.setText(String.valueOf(likeCount));
        commentCountView.setText(String.valueOf(commentCount));
        collectCountView.setText(String.valueOf(collectCount));
        shareCountView.setText(String.valueOf(shareCount));

        likeButton.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        collectButton.setImageResource(collected ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    // -------- 播放 / 暂停 --------

    private void togglePlayPause() {
        if (currentPlayer == null || playIndicator == null) return;

        boolean wasPlaying = currentPlayer.getPlayWhenReady();
        boolean nowPlaying = !wasPlaying;
        currentPlayer.setPlayWhenReady(nowPlaying);

        if (nowPlaying) {
            playIndicator.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                playIndicator.setVisibility(View.GONE);
                playIndicator.setAlpha(0.65f);
            }).start();
        } else {
            playIndicator.setAlpha(0f);
            playIndicator.setVisibility(View.VISIBLE);
            playIndicator.animate().alpha(0.65f).setDuration(120).start();
        }

        setMusicDiscSpinning(nowPlaying);
    }

    // -------- 双击爱心动画 --------

    private void showDoubleTapHeart(float x, float y) {
        if (heartAnimLayer == null || playerView == null) return;

        ImageView heart = new ImageView(requireContext());
        heart.setImageResource(R.drawable.ic_heart_big_filled);

        int size = (int) (120 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        heart.setLayoutParams(lp);

        int[] pvLoc = new int[2];
        int[] layerLoc = new int[2];
        playerView.getLocationOnScreen(pvLoc);
        heartAnimLayer.getLocationOnScreen(layerLoc);

        float layerX = (pvLoc[0] + x) - layerLoc[0];
        float layerY = (pvLoc[1] + y) - layerLoc[1];

        float cx = layerX - size / 2f;
        float cy = layerY - size / 2f;

        if (heartAnimLayer.getWidth() > 0 && heartAnimLayer.getHeight() > 0) {
            cx = Math.max(0, Math.min(cx, heartAnimLayer.getWidth() - size));
            cy = Math.max(0, Math.min(cy, heartAnimLayer.getHeight() - size));
        }

        heart.setX(cx);
        heart.setY(cy);

        heart.setScaleX(0.2f);
        heart.setScaleY(0.2f);
        heart.setAlpha(0f);

        heartAnimLayer.addView(heart);

        float dy = 160 * getResources().getDisplayMetrics().density;

        heart.animate()
                .alpha(1f).scaleX(1.15f).scaleY(1.15f)
                .setDuration(160)
                .withEndAction(() -> heart.animate()
                        .translationYBy(-dy)
                        .alpha(0f)
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(420)
                        .withEndAction(() -> heartAnimLayer.removeView(heart))
                        .start())
                .start();
    }

    // -------- 头像 / 文案 --------

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
                @Override public void onClick(@NonNull View widget) {
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
                        @Override public void onClick(@NonNull View widget) {
                            applyCollapsedText(fullText, false);
                        }
                    }, start, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                descriptionView.setText(builder);
            }
        });
    }

    private void showComments() {
        String id = (feedItem != null) ? feedItem.getId() : "";
        int total = (viewModel != null)
                ? viewModel.getCommentCount(id, safeParseInt(commentCountView.getText().toString()))
                : safeParseInt(commentCountView.getText().toString());

        CommentBottomSheetDialog dialog = CommentBottomSheetDialog.newInstance(id, total);
        dialog.setViewModelFactory(new CommentViewModelFactory(new MockFeedRepository()));

        dialog.setOnCommentAddedListener(newTotal -> {
            if (viewModel != null) {
                viewModel.updateCommentCount(id, newTotal);
            }
        });

        dialog.show(getChildFragmentManager(), "comments");
    }

    private int safeParseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }


    // -------- 转盘 --------

    private void setupMusicDiscRotation() {
        if (musicDiscView == null) return;

        musicDiscAnimator = ObjectAnimator.ofFloat(musicDiscView, View.ROTATION, 0f, 360f);
        musicDiscAnimator.setDuration(6000L);
        musicDiscAnimator.setInterpolator(new LinearInterpolator());
        musicDiscAnimator.setRepeatCount(ValueAnimator.INFINITE);
        musicDiscAnimator.setRepeatMode(ValueAnimator.RESTART);

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

    // -------- 进度条 --------

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

    private void logCover(String where) {
        String frag = (coverView == null)
                ? "null"
                : ("vis=" + coverView.getVisibility() + ",alpha=" + coverView.getAlpha());

        String act = "no-activity";
        if (getActivity() instanceof VideoFeedActivity) {
            act = ((VideoFeedActivity) getActivity()).debugTransitionCoverState();
        } else {
            act = "activity-not-VideoFeedActivity";
        }

        Log.d("COVER", where
                + " pos=" + position
                + " firstFrame=" + firstFrameRendered
                + " fragCover(" + frag + ")"
                + " actCover(" + act + ")"
                + " playWhenReady=" + (currentPlayer != null && currentPlayer.getPlayWhenReady())
                + " state=" + (currentPlayer != null ? currentPlayer.getPlaybackState() : -1));
    }

    private void logPlayerViewLayers(String where) {
        if (playerView == null) {
            Log.d("PV", where + " playerView=null pos=" + position);
            return;
        }
        View shutter = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_shutter);
        ImageView artwork = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_artwork);

        String s1 = (shutter == null) ? "shutter=null"
                : ("shutter(vis=" + shutter.getVisibility() + ",alpha=" + shutter.getAlpha() + ",shown=" + shutter.isShown() + ")");
        String s2 = (artwork == null) ? "artwork=null"
                : ("artwork(vis=" + artwork.getVisibility() + ",alpha=" + artwork.getAlpha() + ",shown=" + artwork.isShown() + ")");

        Log.d("PV", where + " pos=" + position + " " + s1 + " " + s2);
    }

}
