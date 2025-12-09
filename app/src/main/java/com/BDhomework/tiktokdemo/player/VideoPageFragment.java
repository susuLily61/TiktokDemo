package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    public static VideoPageFragment newInstance(FeedItem item) {
        VideoPageFragment fragment = new VideoPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_page, container, false);
        feedItem = (FeedItem) getArguments().getSerializable(ARG_FEED);
        if (feedItem != null) {
            videoUrl = feedItem.getVideoUrl();
        }

        playerView = root.findViewById(R.id.video_player_view);
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
        ImageView pauseIndicator = root.findViewById(R.id.video_pause_indicator);
        ImageButton backButton = root.findViewById(R.id.video_back_button);
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
        }

        gestureDetector = new GestureDetectorCompat(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleLike();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                togglePlayPause(pauseIndicator);
                return super.onSingleTapConfirmed(e);
            }
        });

        root.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

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
        likeButton.setOnClickListener(v -> toggleLike());
        likeContainer.setOnClickListener(v -> toggleLike());
        collectButton.setOnClickListener(v -> toggleCollect());
        collectContainer.setOnClickListener(v -> toggleCollect());
        backButton.setOnClickListener(v -> requireActivity().finish());

        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                isScrubbing = true;
                VideoPlayerManager.getInstance(requireContext()).pause();
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                // no-op
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                isScrubbing = false;
                ExoPlayer player = VideoPlayerManager.getInstance(requireContext()).getPlayer();
                if (player != null) {
                    player.seekTo(position);
                    player.setPlayWhenReady(true);
                }
                scheduleProgressUpdate();
            }
        });

        return root;
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

    private void toggleLike() {
        liked = !liked;
        int baseCount = feedItem != null ? feedItem.getLikeCount() : 0;
        int displayCount = liked ? baseCount + 1 : baseCount;
        likeCountView.setText(String.valueOf(displayCount));
        likeButton.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void toggleCollect() {
        collected = !collected;
        int displayCount = collected ? baseCollectCount + 1 : baseCollectCount;
        collectCountView.setText(String.valueOf(displayCount));
        collectButton.setImageResource(collected ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    private void togglePlayPause(ImageView pauseIndicator) {
        ExoPlayer player = VideoPlayerManager.getInstance(requireContext()).getPlayer();
        if (player == null) return;
        boolean playWhenReady = player.getPlayWhenReady();
        player.setPlayWhenReady(!playWhenReady);
        pauseIndicator.setVisibility(playWhenReady ? View.VISIBLE : View.GONE);
    }

    private void showComments() {
        CommentBottomSheetDialog dialog = new CommentBottomSheetDialog();
        dialog.setOnCommentAddedListener(totalCount -> commentCountView.setText(String.valueOf(Math.max(totalCount, baseCommentCount))));
        dialog.show(getChildFragmentManager(), "comments");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoUrl != null) {
            VideoPlayerManager.getInstance(requireContext()).play(playerView, videoUrl);
            attachPlayerListener();
            scheduleProgressUpdate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VideoPlayerManager.getInstance(requireContext()).pause();
        progressHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VideoPlayerManager.getInstance(requireContext()).detach(playerView);
        playerView = null;
        progressHandler.removeCallbacksAndMessages(null);
    }

    private void attachPlayerListener() {
        if (playerListenerAttached) {
            return;
        }
        ExoPlayer player = VideoPlayerManager.getInstance(requireContext()).getPlayer();
        if (player == null) return;
        player.addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(@NonNull com.google.android.exoplayer2.Timeline timeline, int reason) {
                updateTimeBar();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updateTimeBar();
            }
        });
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
            if (isResumed() && !isScrubbing) {
                progressHandler.postDelayed(this, 500);
            }
        }
    };

    private void updateTimeBar() {
        if (timeBar == null) return;
        ExoPlayer player = VideoPlayerManager.getInstance(requireContext()).getPlayer();
        if (player == null) return;

        long duration = player.getDuration();
        long position = player.getCurrentPosition();
        long buffered = player.getBufferedPosition();
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
