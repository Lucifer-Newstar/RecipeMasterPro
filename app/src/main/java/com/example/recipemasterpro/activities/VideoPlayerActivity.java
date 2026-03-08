package com.example.recipemasterpro.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.utils.VideoPlayerHelper;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class VideoPlayerActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ProgressBar progressBar;
    private TextView stepInfoText;
    private VideoPlayerHelper videoPlayerHelper;

    private String videoUrl;
    private int stepNumber;
    private String instruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get data from intent
        videoUrl = getIntent().getStringExtra("video_url");
        stepNumber = getIntent().getIntExtra("step_number", 1);
        instruction = getIntent().getStringExtra("instruction");

        initViews();
        setupVideoPlayer();
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        stepInfoText = findViewById(R.id.stepInfoText);

        stepInfoText.setText("Step " + stepNumber + ": " + instruction);
    }

    private void setupVideoPlayer() {
        videoPlayerHelper = new VideoPlayerHelper(this, playerView, progressBar);
        videoPlayerHelper.setCallback(new VideoPlayerHelper.VideoPlayerCallback() {
            @Override
            public void onVideoStarted() {
                // Video started playing
            }

            @Override
            public void onVideoCompleted() {
                // Video completed
            }

            @Override
            public void onVideoError(String error) {
                // Handle error
            }
        });

        videoPlayerHelper.initializePlayer(videoUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayerHelper != null) {
            videoPlayerHelper.pausePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayerHelper != null) {
            videoPlayerHelper.resumePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayerHelper != null) {
            videoPlayerHelper.releasePlayer();
        }
    }
}