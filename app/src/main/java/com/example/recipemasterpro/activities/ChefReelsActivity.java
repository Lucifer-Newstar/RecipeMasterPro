package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.ReelsAdapter;
import com.example.recipemasterpro.models.Reel;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.example.recipemasterpro.utils.VideoUploadHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChefReelsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReelsAdapter adapter;
    private List<Reel> reelList;
    private Button uploadReelButton;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private VideoUploadHelper videoUploadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_reels);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        videoUploadHelper = new VideoUploadHelper(this);
        reelList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadMyReels();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        uploadReelButton = findViewById(R.id.uploadReelButton);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        setTitle("My Reels");

        uploadReelButton.setOnClickListener(v -> showUploadReelDialog());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReelsAdapter(this, reelList, new ReelsAdapter.OnReelClickListener() {
            @Override
            public void onReelClick(Reel reel) {
                // Preview reel
                Intent intent = new Intent(ChefReelsActivity.this, ReelPlayerActivity.class);
                intent.putExtra("reelId", reel.getReelId());
                intent.putExtra("videoUrl", reel.getVideoUrl());
                intent.putExtra("title", reel.getTitle());
                intent.putExtra("chefName", reel.getChefName());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Reel reel, int position) {
                // Chefs can't like their own reels? Or they can?
                Toast.makeText(ChefReelsActivity.this,
                        "View likes: " + reel.getLikes(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCommentClick(Reel reel) {
                showDeleteReelDialog(reel);
            }

            @Override
            public void onShareClick(Reel reel) {
                shareReel(reel);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadMyReels() {
        String chefId = sessionManager.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.REELS_COLLECTION)
                .whereEqualTo("chefId", chefId)
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

    private void showUploadReelDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_reel, null);
        EditText titleInput = dialogView.findViewById(R.id.reelTitleInput);
        EditText descriptionInput = dialogView.findViewById(R.id.reelDescriptionInput);
        Button selectVideoButton = dialogView.findViewById(R.id.selectVideoButton);
        TextView selectedVideoText = dialogView.findViewById(R.id.selectedVideoText);
        ProgressBar uploadProgress = dialogView.findViewById(R.id.uploadProgress);

        final Uri[] selectedVideoUri = {null};

        ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedVideoUri[0] = result.getData().getData();
                            selectedVideoText.setText(selectedVideoUri[0].getLastPathSegment());
                        }
                    }
                });

        selectVideoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            videoPickerLauncher.launch(intent);
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Upload New Reel")
                .setView(dialogView)
                .setPositiveButton("Upload", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedVideoUri[0] == null) {
                        Toast.makeText(this, "Please select a video", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uploadReel(title, description, selectedVideoUri[0], uploadProgress);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadReel(String title, String description, Uri videoUri, ProgressBar uploadProgress) {
        uploadProgress.setVisibility(View.VISIBLE);

        videoUploadHelper.uploadVideo(videoUri, "reels", new VideoUploadHelper.VideoUploadCallback() {
            @Override
            public void onUploadSuccess(String videoUrl) {
                saveReelToFirestore(title, description, videoUrl, uploadProgress);
            }

            @Override
            public void onUploadFailure(String error) {
                uploadProgress.setVisibility(View.GONE);
                Toast.makeText(ChefReelsActivity.this,
                        "Upload failed: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(int progress) {
                uploadProgress.setProgress(progress);
            }
        });
    }

    private void saveReelToFirestore(String title, String description, String videoUrl,
                                     ProgressBar uploadProgress) {
        String chefId = sessionManager.getUserId();
        String chefName = "Chef Name"; // You should get this from user data

        Reel reel = new Reel(null, chefId, chefName, title, videoUrl);
        reel.setDescription(description);

        db.collection(Constants.REELS_COLLECTION)
                .add(reel)
                .addOnSuccessListener(documentReference -> {
                    uploadProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Reel uploaded successfully!", Toast.LENGTH_SHORT).show();
                    loadMyReels();
                })
                .addOnFailureListener(e -> {
                    uploadProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to save reel: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteReelDialog(Reel reel) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Reel")
                .setMessage("Are you sure you want to delete this reel?")
                .setPositiveButton("Delete", (dialog, which) -> deleteReel(reel))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReel(Reel reel) {
        db.collection(Constants.REELS_COLLECTION)
                .document(reel.getReelId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    videoUploadHelper.deleteVideo(reel.getVideoUrl());
                    reelList.remove(reel);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Reel deleted", Toast.LENGTH_SHORT).show();

                    if (reelList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete reel: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void shareReel(Reel reel) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my cooking reel!");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Watch my cooking reel: " + reel.getTitle() +
                        " on RecipeMaster Pro!");
        startActivity(Intent.createChooser(shareIntent, "Share Reel"));
    }
}
