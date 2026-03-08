package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.ReelsAdapter;
import com.example.recipemasterpro.models.Reel;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReelsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReelsAdapter adapter;
    private List<Reel> reelList;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        db = FirebaseFirestore.getInstance();
        reelList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadReels();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        setTitle("Cooking Reels");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReelsAdapter(this, reelList, new ReelsAdapter.OnReelClickListener() {
            @Override
            public void onReelClick(Reel reel) {
                // Open reel player
                Intent intent = new Intent(ReelsActivity.this, ReelPlayerActivity.class);
                intent.putExtra("reelId", reel.getReelId());
                intent.putExtra("videoUrl", reel.getVideoUrl());
                intent.putExtra("title", reel.getTitle());
                intent.putExtra("chefName", reel.getChefName());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Reel reel, int position) {
                toggleLike(reel, position);
            }

            @Override
            public void onCommentClick(Reel reel) {
                Toast.makeText(ReelsActivity.this, "Comments coming soon!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onShareClick(Reel reel) {
                shareReel(reel);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadReels() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.REELS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    reelList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reel reel = document.toObject(Reel.class);
                        reel.setReelId(document.getId());
                        reelList.add(reel);
                    }

                    adapter.notifyDataSetChanged();

                    if (reelList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading reels: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleLike(Reel reel, int position) {
        int newLikes = reel.getLikes() + 1;
        reel.setLikes(newLikes);

        db.collection(Constants.REELS_COLLECTION)
                .document(reel.getReelId())
                .update("likes", newLikes)
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to like reel", Toast.LENGTH_SHORT).show();
                });
    }

    private void shareReel(Reel reel) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe reel!");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Watch this amazing cooking reel: " + reel.getTitle() +
                        " by " + reel.getChefName() +
                        " on RecipeMaster Pro!");
        startActivity(Intent.createChooser(shareIntent, "Share Reel"));
    }
}