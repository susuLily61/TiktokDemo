package com.BDhomework.tiktokdemo.player;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.DefaultLoadControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralized manager for handling ExoPlayer lifecycle and reuse across pages.
 */
public class VideoPlayerManager {

    private static VideoPlayerManager sInstance;

    private final Context appContext;
    private final List<ExoPlayer> players = new ArrayList<>(3);
    private final Map<Integer, ExoPlayer> positionToPlayer = new HashMap<>();
    private final LoadControl loadControl;

    private VideoPlayerManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(5000, 10000, 1500, 2000)
                .build();
        initializePlayers();
    }

    public static synchronized VideoPlayerManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VideoPlayerManager(context);
        }
        return sInstance;
    }

    public synchronized ExoPlayer acquirePlayerForPosition(int position) {
        if (positionToPlayer.containsKey(position)) {
            return positionToPlayer.get(position);
        }

        ExoPlayer available = findAvailablePlayer();
        if (available == null) {
            return null;
        }
        available.stop();
        available.clearMediaItems();
        available.clearVideoSurface();
        positionToPlayer.put(position, available);
        return available;
    }

    public synchronized void releasePosition(int position) {
        ExoPlayer player = positionToPlayer.remove(position);
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.clearMediaItems();
            player.clearVideoSurface();
        }
    }

    public synchronized void play(PlayerView playerView, String url, int position) {
        prepare(playerView, url, position, true);
    }

    public synchronized ExoPlayer prepare(PlayerView playerView, String url, int position, boolean playWhenReady) {
        ExoPlayer player = acquirePlayerForPosition(position);
        if (player == null) {
            return null;
        }
        attachPlayerView(playerView, player);
        player.setMediaItem(MediaItem.fromUri(url));
        player.setPlayWhenReady(playWhenReady);
        player.prepare();
        return player;
    }

    public synchronized void pauseAll() {
        for (ExoPlayer player : players) {
            player.setPlayWhenReady(false);
        }
    }

    public synchronized void release() {
        for (ExoPlayer player : players) {
            player.release();
        }
        players.clear();
        positionToPlayer.clear();
    }

    public synchronized Set<Integer> getBoundPositions() {
        return new HashSet<>(positionToPlayer.keySet());
    }

    private void initializePlayers() {
        for (int i = 0; i < 3; i++) {
            ExoPlayer player = new ExoPlayer.Builder(appContext)
                    .setLoadControl(loadControl)
                    .build();
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            players.add(player);
        }
    }

    private ExoPlayer findAvailablePlayer() {
        Set<ExoPlayer> inUse = new HashSet<>(positionToPlayer.values());
        for (ExoPlayer player : players) {
            if (!inUse.contains(player)) {
                return player;
            }
        }
        return null;
    }

    private void attachPlayerView(PlayerView playerView, ExoPlayer player) {
        if (playerView == null) {
            return;
        }
        playerView.setPlayer(player);
    }
    @Nullable
    public synchronized ExoPlayer getPlayerForPosition(int position) {
        return positionToPlayer.get(position);
    }

}
