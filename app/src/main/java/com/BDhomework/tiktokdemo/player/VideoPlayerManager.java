package com.BDhomework.tiktokdemo.player;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

/**
 * Centralized manager for handling ExoPlayer lifecycle and reuse across pages.
 */
public class VideoPlayerManager {

    private static VideoPlayerManager sInstance;

    private final Context appContext;
    private ExoPlayer player;
    private PlayerView currentPlayerView;
    private String currentUrl;

    private VideoPlayerManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized VideoPlayerManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VideoPlayerManager(context);
        }
        return sInstance;
    }

    public void play(PlayerView playerView, String url) {
        ensurePlayer();
        attachPlayerView(playerView);
        boolean needNewSource = currentUrl == null || !currentUrl.equals(url);
        if (needNewSource) {
            player.setMediaItem(MediaItem.fromUri(url));
            currentUrl = url;
            player.prepare();
        }
        player.setPlayWhenReady(true);
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void detach(PlayerView playerView) {
        if (currentPlayerView == playerView) {
            currentPlayerView.setPlayer(null);
            currentPlayerView = null;
        } else if (playerView != null) {
            playerView.setPlayer(null);
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (currentPlayerView != null) {
            currentPlayerView.setPlayer(null);
            currentPlayerView = null;
        }
        currentUrl = null;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    private void ensurePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(appContext).build();
        }
    }

    private void attachPlayerView(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        if (currentPlayerView != null && currentPlayerView != playerView) {
            currentPlayerView.setPlayer(null);
        }
        currentPlayerView = playerView;
        currentPlayerView.setPlayer(player);
    }
}
