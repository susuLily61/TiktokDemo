package com.BDhomework.tiktokdemo.player;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.BDhomework.tiktokdemo.R;
import com.BDhomework.tiktokdemo.comment.CommentBottomSheetDialog;
import com.BDhomework.tiktokdemo.model.FeedItem;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoPageFragment extends Fragment {

    private static final String ARG_FEED = "arg_feed";

    private FeedItem feedItem;
    private ExoPlayer player;
    private PlayerView playerView;
    private TextView likeCountView;
    private boolean liked = false;
    private GestureDetectorCompat gestureDetector;

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

        playerView = root.findViewById(R.id.video_player_view);
        TextView title = root.findViewById(R.id.video_title);
        TextView author = root.findViewById(R.id.video_author);
        likeCountView = root.findViewById(R.id.video_like_count);
        ImageButton commentButton = root.findViewById(R.id.video_comment_button);
        ImageButton shareButton = root.findViewById(R.id.video_share_button);
        ImageButton likeButton = root.findViewById(R.id.video_like_button);
        ImageView pauseIndicator = root.findViewById(R.id.video_pause_indicator);

        if (feedItem != null) {
            title.setText(feedItem.getTitle());
            author.setText("@" + feedItem.getAuthorName());
            likeCountView.setText(String.valueOf(feedItem.getLikeCount()));
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

        commentButton.setOnClickListener(v -> new CommentBottomSheetDialog().show(getChildFragmentManager(), "comments"));
        shareButton.setOnClickListener(v -> Toast.makeText(requireContext(), "分享功能待接入", Toast.LENGTH_SHORT).show());
        likeButton.setOnClickListener(v -> toggleLike());

        preparePlayer();
        return root;
    }

    private void toggleLike() {
        liked = !liked;
        int baseCount = feedItem != null ? feedItem.getLikeCount() : 0;
        int displayCount = liked ? baseCount + 1 : baseCount;
        likeCountView.setText(String.valueOf(displayCount));
    }

    private void togglePlayPause(ImageView pauseIndicator) {
        if (player == null) return;
        boolean playWhenReady = player.getPlayWhenReady();
        player.setPlayWhenReady(!playWhenReady);
        pauseIndicator.setVisibility(playWhenReady ? View.VISIBLE : View.GONE);
    }

    private void preparePlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(player);
        if (feedItem != null) {
            MediaItem mediaItem = MediaItem.fromUri(feedItem.getVideoUrl());
            player.setMediaItem(mediaItem);
        }
        player.setPlayWhenReady(true);
        player.prepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
