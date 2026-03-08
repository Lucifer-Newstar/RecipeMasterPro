package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.ProfileRecipeAdapter;
import com.example.recipemasterpro.adapters.ProfileReelAdapter;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.models.Reel;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.ImageUploadHelper;
import com.example.recipemasterpro.utils.NotificationHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView profileImage, coverImage, editProfileButton;
    private TextView userNameText, userBioText, userLocationText, joinDateText;
    private TextView recipesCountText, reelsCountText, followersCountText, followingCountText;
    private Button editProfileBtn, followButton, messageButton;
    private RecyclerView contentRecyclerView;
    private TabLayout tabLayout;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private ImageUploadHelper imageUploadHelper;
    private NotificationHelper notificationHelper;
    private String profileUserId;
    private boolean isOwnProfile;
    private User profileUser;
    private List<Recipe> recipeList;
    private List<Reel> reelList;
    private ProfileRecipeAdapter recipeAdapter;
    private ProfileReelAdapter reelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        imageUploadHelper = new ImageUploadHelper(this);
        notificationHelper = new NotificationHelper();

        // Get user ID from intent (if viewing someone else's profile)
        profileUserId = getIntent().getStringExtra("userId");
        if (profileUserId == null) {
            profileUserId = sessionManager.getUserId();
        }

        isOwnProfile = profileUserId != null && profileUserId.equals(sessionManager.getUserId());

        recipeList = new ArrayList<>();
        reelList = new ArrayList<>();

        initViews();
        setupAdapters();
        setupClickListeners();

        if (profileUserId != null) {
            loadUserProfile();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        coverImage = findViewById(R.id.coverImage);
        userNameText = findViewById(R.id.userNameText);
        userBioText = findViewById(R.id.userBioText);
        userLocationText = findViewById(R.id.userLocationText);
        joinDateText = findViewById(R.id.joinDateText);
        recipesCountText = findViewById(R.id.recipesCountText);
        reelsCountText = findViewById(R.id.reelsCountText);
        followersCountText = findViewById(R.id.followersCountText);
        followingCountText = findViewById(R.id.followingCountText);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        followButton = findViewById(R.id.followButton);
        messageButton = findViewById(R.id.messageButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        contentRecyclerView = findViewById(R.id.contentRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);
        progressBar = findViewById(R.id.progressBar);

        // Show/hide buttons based on own profile
        if (isOwnProfile) {
            editProfileBtn.setVisibility(View.VISIBLE);
            editProfileButton.setVisibility(View.VISIBLE);
            followButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.GONE);
        } else {
            editProfileBtn.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE);
            messageButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapters() {
        recipeAdapter = new ProfileRecipeAdapter(this, recipeList, recipe -> {
            startActivity(new Intent(this, RecipeDetailActivity.class)
                    .putExtra("recipeId", recipe.getRecipeId()));
        });

        reelAdapter = new ProfileReelAdapter(this, reelList, reel -> {
            startActivity(new Intent(this, ReelPlayerActivity.class)
                    .putExtra("reelId", reel.getReelId())
                    .putExtra("videoUrl", reel.getVideoUrl())
                    .putExtra("title", reel.getTitle()));
        });

        // Setup RecyclerView with grid layout (2 columns)
        contentRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupClickListeners() {
        editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        followButton.setOnClickListener(v -> {
            toggleFollow();
        });

        messageButton.setOnClickListener(v -> {
            Toast.makeText(this, "Messaging coming soon!", Toast.LENGTH_SHORT).show();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showRecipes();
                } else {
                    showReels();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Profile image click (only for own profile)
        if (isOwnProfile) {
            profileImage.setOnClickListener(v -> {
                pickImage(ImageUploadHelper.IMAGE_TYPE_PROFILE);
            });

            coverImage.setOnClickListener(v -> {
                pickImage(ImageUploadHelper.IMAGE_TYPE_COVER);
            });
        }
    }

    private void pickImage(String type) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Toast.makeText(this, "Please use Edit Profile to change images", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.USERS_COLLECTION)
                .document(profileUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    profileUser = documentSnapshot.toObject(User.class);
                    if (profileUser != null) {
                        displayUserInfo();
                        loadUserContent();
                        checkFollowStatus();
                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayUserInfo() {
        userNameText.setText(profileUser.getName());

        if (profileUser.getBio() != null && !profileUser.getBio().isEmpty()) {
            userBioText.setText(profileUser.getBio());
            userBioText.setVisibility(View.VISIBLE);
        } else {
            userBioText.setVisibility(View.GONE);
        }

        if (profileUser.getLocation() != null && !profileUser.getLocation().isEmpty()) {
            userLocationText.setText("📍 " + profileUser.getLocation());
            userLocationText.setVisibility(View.VISIBLE);
        } else {
            userLocationText.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        joinDateText.setText("Joined " + sdf.format(new Date(profileUser.getCreatedAt())));

        recipesCountText.setText(String.valueOf(profileUser.getRecipeCount()));
        reelsCountText.setText(String.valueOf(profileUser.getReelCount()));
        followersCountText.setText(String.valueOf(profileUser.getFollowersCount()));
        followingCountText.setText(String.valueOf(profileUser.getFollowingCount()));

        // Load profile image
        if (profileUser.getProfileImageUrl() != null && !profileUser.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(profileUser.getProfileImageUrl())
                    .placeholder(R.drawable.ic_chef_placeholder)
                    .circleCrop()
                    .into(profileImage);
        }

        // Load cover image
        if (profileUser.getCoverImageUrl() != null && !profileUser.getCoverImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(profileUser.getCoverImageUrl())
                    .placeholder(R.color.primary_light)
                    .centerCrop()
                    .into(coverImage);
        }
    }

    private void checkFollowStatus() {
        String currentUserId = sessionManager.getUserId();
        if (currentUserId == null || profileUser == null || isOwnProfile) return;

        boolean isFollowing = profileUser.getFollowers() != null &&
                profileUser.getFollowers().contains(currentUserId);

        if (isFollowing) {
            followButton.setText("Following");
            followButton.setBackgroundTintList(getColorStateList(R.color.accent));
        } else {
            followButton.setText("Follow");
            followButton.setBackgroundTintList(getColorStateList(R.color.accent));
        }
    }

    private void loadUserContent() {
        // Load recipes
        db.collection(Constants.RECIPES_COLLECTION)
                .whereEqualTo("chefId", profileUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        recipe.setRecipeId(doc.getId());
                        recipeList.add(recipe);
                    }
                    recipeAdapter.notifyDataSetChanged();

                    // Update recipe count if different
                    if (profileUser.getRecipeCount() != recipeList.size()) {
                        updateRecipeCount(recipeList.size());
                    }

                    showRecipes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                });

        // Load reels
        db.collection(Constants.REELS_COLLECTION)
                .whereEqualTo("chefId", profileUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reelList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reel reel = doc.toObject(Reel.class);
                        reel.setReelId(doc.getId());
                        reelList.add(reel);
                    }
                    reelAdapter.notifyDataSetChanged();

                    // Update reel count if different
                    if (profileUser.getReelCount() != reelList.size()) {
                        updateReelCount(reelList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading reels", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecipeCount(int count) {
        db.collection(Constants.USERS_COLLECTION)
                .document(profileUserId)
                .update("recipeCount", count);
        recipesCountText.setText(String.valueOf(count));
    }

    private void updateReelCount(int count) {
        db.collection(Constants.USERS_COLLECTION)
                .document(profileUserId)
                .update("reelCount", count);
        reelsCountText.setText(String.valueOf(count));
    }

    private void showRecipes() {
        contentRecyclerView.setAdapter(recipeAdapter);
    }

    private void showReels() {
        contentRecyclerView.setAdapter(reelAdapter);
    }

    private void toggleFollow() {
        String currentUserId = sessionManager.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Please login to follow", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId.equals(profileUserId)) {
            Toast.makeText(this, "You cannot follow yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFollowing = profileUser.getFollowers() != null &&
                profileUser.getFollowers().contains(currentUserId);

        progressBar.setVisibility(View.VISIBLE);
        followButton.setEnabled(false);

        if (isFollowing) {
            unfollowUser(currentUserId);
        } else {
            followUser(currentUserId);
        }
    }

    private void followUser(String currentUserId) {
        // Add current user to profile's followers
        db.collection(Constants.USERS_COLLECTION)
                .document(profileUserId)
                .update("followers", FieldValue.arrayUnion(currentUserId),
                        "followersCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    // Add profile to current user's following
                    db.collection(Constants.USERS_COLLECTION)
                            .document(currentUserId)
                            .update("following", FieldValue.arrayUnion(profileUserId),
                                    "followingCount", FieldValue.increment(1))
                            .addOnSuccessListener(aVoid2 -> {
                                progressBar.setVisibility(View.GONE);
                                followButton.setEnabled(true);
                                followButton.setText("Following");
                                followButton.setBackgroundTintList(getColorStateList(R.color.accent));

                                // Update local data
                                if (profileUser.getFollowers() == null) {
                                    profileUser.setFollowers(new ArrayList<>());
                                }
                                profileUser.getFollowers().add(currentUserId);
                                profileUser.setFollowersCount(profileUser.getFollowersCount() + 1);
                                followersCountText.setText(String.valueOf(profileUser.getFollowersCount()));

                                // Send follow notification
                                String currentUserName = sessionManager.getUserName();
                                String message = currentUserName + " started following you";

                                notificationHelper.sendNotification(
                                        profileUserId,  // user being followed
                                        currentUserId,  // follower
                                        currentUserName,
                                        Constants.NOTIFICATION_TYPE_FOLLOW,
                                        message,
                                        null, null,
                                        null
                                );

                                // Send push notification
                                sendPushNotification(profileUserId, message, Constants.NOTIFICATION_TYPE_FOLLOW, null);

                                Toast.makeText(this, "You are now following " + profileUser.getName(),
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                followButton.setEnabled(true);
                                Toast.makeText(this, "Error updating following list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    followButton.setEnabled(true);
                    Toast.makeText(this, "Error following user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendPushNotification(String userId, String message, String type, String recipeId) {
        db.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fcmToken = documentSnapshot.getString("fcmToken");
                        if (fcmToken != null && !fcmToken.isEmpty()) {
                            // In a real app, you would call your backend or Firebase Functions here
                            // to send the actual FCM message.
                            Log.d(TAG, "Sending push notification to token: " + fcmToken);
                            Log.d(TAG, "Notification content: " + message);
                        }
                    }
                });
    }

    private void unfollowUser(String currentUserId) {
        // Remove current user from profile's followers
        db.collection(Constants.USERS_COLLECTION)
                .document(profileUserId)
                .update("followers", FieldValue.arrayRemove(currentUserId),
                        "followersCount", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    // Remove profile from current user's following
                    db.collection(Constants.USERS_COLLECTION)
                            .document(currentUserId)
                            .update("following", FieldValue.arrayRemove(profileUserId),
                                    "followingCount", FieldValue.increment(-1))
                            .addOnSuccessListener(aVoid2 -> {
                                progressBar.setVisibility(View.GONE);
                                followButton.setEnabled(true);
                                followButton.setText("Follow");
                                followButton.setBackgroundTintList(getColorStateList(R.color.accent));

                                // Update local data
                                if (profileUser.getFollowers() != null) {
                                    profileUser.getFollowers().remove(currentUserId);
                                }
                                profileUser.setFollowersCount(Math.max(0, profileUser.getFollowersCount() - 1));
                                followersCountText.setText(String.valueOf(profileUser.getFollowersCount()));

                                Toast.makeText(this, "You unfollowed " + profileUser.getName(),
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                followButton.setEnabled(true);
                                Toast.makeText(this, "Error updating following list", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    followButton.setEnabled(true);
                    Toast.makeText(this, "Error unfollowing user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (profileUserId != null) {
            loadUserProfile();
        }
    }
}