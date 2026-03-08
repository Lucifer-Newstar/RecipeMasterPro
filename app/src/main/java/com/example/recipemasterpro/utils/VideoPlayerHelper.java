package com.example.recipemasterpro.utils;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class VideoPlayerHelper {

    private ExoPlayer player;
    private Context context;
    private StyledPlayerView playerView;
    private ProgressBar progressBar;
    private VideoPlayerCallback callback;

    public interface VideoPlayerCallback {
        void onVideoStarted();
        void onVideoCompleted();
        void onVideoError(String error);
    }

    public VideoPlayerHelper(Context context, StyledPlayerView playerView, ProgressBar progressBar) {
        this.context = context;
        this.playerView = playerView;
        this.progressBar = progressBar;
    }

    public void setCallback(VideoPlayerCallback callback) {
        this.callback = callback;
    }

    public void initializePlayer(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            if (callback != null) {
                callback.onVideoError("No video URL provided");
            }
            return;
        }

        try {
            player = new ExoPlayer.Builder(context).build();
            playerView.setPlayer(player);

            // Set up media item
            Uri videoUri = Uri.parse(videoUrl);
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);

            // Add listener for player events
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                            if (progressBar != null) {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                            break;
                        case Player.STATE_READY:
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (callback != null) {
                                callback.onVideoStarted();
                            }
                            break;
                        case Player.STATE_ENDED:
                            if (callback != null) {
                                callback.onVideoCompleted();
                            }
                            break;
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (callback != null) {
                        callback.onVideoError("Playback error: " + error.getMessage());
                    }
                }
            });

            player.prepare();
            player.setPlayWhenReady(true);

        } catch (Exception e) {
            if (callback != null) {
                callback.onVideoError("Failed to initialize player: " + e.getMessage());
            }
        }
    }

    public void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }
}