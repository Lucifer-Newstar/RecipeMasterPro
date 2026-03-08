package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.ImageUploadHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImage, coverImage;
    private EditText nameEditText, bioEditText, locationEditText, websiteEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;
    private TextView profileProgressText, coverProgressText;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private ImageUploadHelper imageUploadHelper;
    private String userId;

    private Uri newProfileImageUri;
    private Uri newCoverImageUri;
    private String currentProfileImageUrl;
    private String currentCoverImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        imageUploadHelper = new ImageUploadHelper(this);
        userId = sessionManager.getUserId();

        initViews();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        coverImage = findViewById(R.id.coverImage);
        nameEditText = findViewById(R.id.nameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        locationEditText = findViewById(R.id.locationEditText);
        websiteEditText = findViewById(R.id.websiteEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);
        profileProgressText = findViewById(R.id.profileProgressText);
        coverProgressText = findViewById(R.id.coverProgressText);

        setTitle("Edit Profile");
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(v -> {
            pickImage(ImageUploadHelper.IMAGE_TYPE_PROFILE);
        });

        coverImage.setOnClickListener(v -> {
            pickImage(ImageUploadHelper.IMAGE_TYPE_COVER);
        });

        saveButton.setOnClickListener(v -> {
            saveProfile();
        });

        cancelButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void pickImage(String type) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        if (type.equals(ImageUploadHelper.IMAGE_TYPE_PROFILE)) {
            profileImagePickerLauncher.launch(intent);
        } else {
            coverImagePickerLauncher.launch(intent);
        }
    }

    ActivityResultLauncher<Intent> profileImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    newProfileImageUri = result.getData().getData();
                    // Preview the image
                    if (newProfileImageUri != null) {
                        Glide.with(this)
                                .load(newProfileImageUri)
                                .circleCrop()
                                .into(profileImage);
                        profileProgressText.setVisibility(View.VISIBLE);
                        profileProgressText.setText("Image selected. Save to upload.");
                    }
                }
            });

    ActivityResultLauncher<Intent> coverImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    newCoverImageUri = result.getData().getData();
                    // Preview the image
                    if (newCoverImageUri != null) {
                        Glide.with(this)
                                .load(newCoverImageUri)
                                .centerCrop()
                                .into(coverImage);
                        coverProgressText.setVisibility(View.VISIBLE);
                        coverProgressText.setText("Image selected. Save to upload.");
                    }
                }
            });

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        nameEditText.setText(user.getName());
                        bioEditText.setText(user.getBio() != null ? user.getBio() : "");
                        locationEditText.setText(user.getLocation() != null ? user.getLocation() : "");
                        websiteEditText.setText(user.getWebsite() != null ? user.getWebsite() : "");

                        currentProfileImageUrl = user.getProfileImageUrl();
                        currentCoverImageUrl = user.getCoverImageUrl();

                        // Load profile image
                        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentProfileImageUrl)
                                    .placeholder(R.drawable.ic_chef_placeholder)
                                    .circleCrop()
                                    .into(profileImage);
                        }

                        // Load cover image
                        if (currentCoverImageUrl != null && !currentCoverImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentCoverImageUrl)
                                    .placeholder(R.color.primary_light)
                                    .centerCrop()
                                    .into(coverImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String website = websiteEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        // If there are new images, upload them first
        if (newProfileImageUri != null || newCoverImageUri != null) {
            uploadImagesAndSave(name, bio, location, website);
        } else {
            updateUserData(name, bio, location, website, null, null);
        }
    }

    private void uploadImagesAndSave(String name, String bio, String location, String website) {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        final String[] newProfileUrl = {currentProfileImageUrl};
        final String[] newCoverUrl = {currentCoverImageUrl};
        final int[] completedUploads = {0};
        int totalUploads = (newProfileImageUri != null ? 1 : 0) + (newCoverImageUri != null ? 1 : 0);

        if (newProfileImageUri != null) {
            imageUploadHelper.uploadImage(newProfileImageUri, ImageUploadHelper.IMAGE_TYPE_PROFILE,
                    userId, new ImageUploadHelper.ImageUploadCallback() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            newProfileUrl[0] = imageUrl;
                            completedUploads[0]++;
                            profileProgressText.setText("Upload complete!");
                            if (completedUploads[0] == totalUploads) {
                                updateUserData(name, bio, location, website,
                                        newProfileUrl[0], newCoverUrl[0]);
                            }
                        }

                        @Override
                        public void onUploadFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            saveButton.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this,
                                    "Profile image upload failed: " + error, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProgress(int progress) {
                            profileProgressText.setVisibility(View.VISIBLE);
                            profileProgressText.setText("Uploading profile: " + progress + "%");
                        }
                    });
        }

        if (newCoverImageUri != null) {
            imageUploadHelper.uploadImage(newCoverImageUri, ImageUploadHelper.IMAGE_TYPE_COVER,
                    userId, new ImageUploadHelper.ImageUploadCallback() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            newCoverUrl[0] = imageUrl;
                            completedUploads[0]++;
                            coverProgressText.setText("Upload complete!");
                            if (completedUploads[0] == totalUploads) {
                                updateUserData(name, bio, location, website,
                                        newProfileUrl[0], newCoverUrl[0]);
                            }
                        }

                        @Override
                        public void onUploadFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            saveButton.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this,
                                    "Cover image upload failed: " + error, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProgress(int progress) {
                            coverProgressText.setVisibility(View.VISIBLE);
                            coverProgressText.setText("Uploading cover: " + progress + "%");
                        }
                    });
        }
    }

    private void updateUserData(String name, String bio, String location, String website,
                                String profileImageUrl, String coverImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("location", location);
        updates.put("website", website);

        if (profileImageUrl != null) {
            updates.put("profileImageUrl", profileImageUrl);
            // Delete old profile image if it exists
            if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()
                    && !currentProfileImageUrl.equals(profileImageUrl)) {
                imageUploadHelper.deleteImage(currentProfileImageUrl);
            }
        }

        if (coverImageUrl != null) {
            updates.put("coverImageUrl", coverImageUrl);
            // Delete old cover image if it exists
            if (currentCoverImageUrl != null && !currentCoverImageUrl.isEmpty()
                    && !currentCoverImageUrl.equals(coverImageUrl)) {
                imageUploadHelper.deleteImage(currentCoverImageUrl);
            }
        }

        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Error updating profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}