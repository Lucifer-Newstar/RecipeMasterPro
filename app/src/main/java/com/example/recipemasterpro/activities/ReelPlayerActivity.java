package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Reel;
import com.example.recipemasterpro.utils.Constants;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReelPlayerActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private TextView titleText, chefNameText, likesText, commentsText;
    private ImageView likeButton, commentButton, shareButton, profileImage;
    private String videoUrl, reelId, title, chefName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel_player);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        videoUrl = getIntent().getStringExtra("videoUrl");
        reelId = getIntent().getStringExtra("reelId");
        title = getIntent().getStringExtra("title");
        chefName = getIntent().getStringExtra("chefName");

        initViews();
        setupPlayer();
        loadReelDetails();
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        titleText = findViewById(R.id.titleText);
        chefNameText = findViewById(R.id.chefNameText);
        likesText = findViewById(R.id.likesText);
        commentsText = findViewById(R.id.commentsText);
        likeButton = findViewById(R.id.likeButton);
        commentButton = findViewById(R.id.commentButton);
        shareButton = findViewById(R.id.shareButton);
        profileImage = findViewById(R.id.profileImage);

        titleText.setText(title);
        chefNameText.setText(chefName);
        profileImage.setImageResource(R.drawable.ic_chef_placeholder);

        setupClickListeners();
    }

    private void setupClickListeners() {
        likeButton.setOnClickListener(v -> {
            Toast.makeText(this, "Like feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        commentButton.setOnClickListener(v -> {
            Toast.makeText(this, "Comments coming soon!", Toast.LENGTH_SHORT).show();
        });

        shareButton.setOnClickListener(v -> {
            shareReel();
        });
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    incrementViewCount();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReelPlayerActivity.this,
                        "Error playing video", Toast.LENGTH_SHORT).show();
            }
        });

        player.prepare();
        player.setPlayWhenReady(true);
    }

    private void loadReelDetails() {
        db.collection(Constants.REELS_COLLECTION)
                .document(reelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Reel reel = documentSnapshot.toObject(Reel.class);
                    if (reel != null) {
                        likesText.setText(String.valueOf(reel.getLikes()));
                        commentsText.setText(String.valueOf(reel.getComments()));
                    }
                });
    }

    private void incrementViewCount() {
        db.collection(Constants.REELS_COLLECTION)
                .document(reelId)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1));
    }

    private void shareReel() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe reel!");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Watch this amazing cooking reel: " + title +
                        " by " + chefName +
                        " on RecipeMaster Pro!");
        startActivity(Intent.createChooser(shareIntent, "Share Reel"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
