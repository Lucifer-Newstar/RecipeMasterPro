package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.CommentAdapter;
import com.example.recipemasterpro.adapters.IngredientAdapter;
import com.example.recipemasterpro.adapters.RatingsAdapter;
import com.example.recipemasterpro.adapters.StepAdapter;
import com.example.recipemasterpro.models.Comment;
import com.example.recipemasterpro.models.Rating;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.NotificationHelper;
import com.example.recipemasterpro.utils.ReportHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.example.recipemasterpro.utils.StatisticsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
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

public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    // UI Components
    private ImageView recipeImage;
    private TextView titleText, chefText, descriptionText, timeText, servingsText;
    private RatingBar ratingBar;
    private TextView ratingCountText, noIngredientsText, noStepsText;
    private RecyclerView ingredientsRecyclerView, stepsRecyclerView, ratingsRecyclerView;
    private ProgressBar progressBar;

    // Comments Section
    private LinearLayout commentsSection;
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button postCommentButton;
    private TextView commentCountText, noCommentsText;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    // Role-based buttons
    private LinearLayout chefButtonLayout, userButtonLayout;
    private Button editButton, deleteButton, statsButton;
    private Button rateButton, likeButton, favoriteButton, shareButton, reportButton;

    // Firebase
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private StatisticsHelper statisticsHelper;
    private NotificationHelper notificationHelper;
    private ReportHelper reportHelper;

    // Data
    private String recipeId;
    private Recipe currentRecipe;
    private String currentUserRole;
    private String currentUserId;
    private List<Rating> ratingList;
    private RatingsAdapter ratingsAdapter;

    // Like tracking
    private boolean isLiked = false;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        statisticsHelper = new StatisticsHelper();
        notificationHelper = new NotificationHelper();
        reportHelper = new ReportHelper(this);

        // Get current user info
        currentUserRole = sessionManager.getUserRole();
        currentUserId = sessionManager.getUserId();

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipeId");

        initViews();
        setupCommentAdapter();
        setupRoleBasedUI();

        if (recipeId != null) {
            loadRecipeDetails();
            loadRatings();
            loadComments();
            checkUserInteraction();
        } else {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        recipeImage = findViewById(R.id.recipeImage);
        titleText = findViewById(R.id.titleText);
        chefText = findViewById(R.id.chefText);
        descriptionText = findViewById(R.id.descriptionText);
        timeText = findViewById(R.id.timeText);
        servingsText = findViewById(R.id.servingsText);
        ratingBar = findViewById(R.id.ratingBar);
        ratingCountText = findViewById(R.id.ratingCountText);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        ratingsRecyclerView = findViewById(R.id.ratingsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        noIngredientsText = findViewById(R.id.noIngredientsText);
        noStepsText = findViewById(R.id.noStepsText);

        // Comments Section
        commentsSection = findViewById(R.id.commentsSection);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        postCommentButton = findViewById(R.id.postCommentButton);
        commentCountText = findViewById(R.id.commentCountText);
        noCommentsText = findViewById(R.id.noCommentsText);
        commentList = new ArrayList<>();

        // Role-based layouts
        chefButtonLayout = findViewById(R.id.chefButtonLayout);
        userButtonLayout = findViewById(R.id.userButtonLayout);

        // Chef buttons
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        statsButton = findViewById(R.id.statsButton);

        // User buttons
        rateButton = findViewById(R.id.rateButton);
        likeButton = findViewById(R.id.likeButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        shareButton = findViewById(R.id.shareButton);
        reportButton = findViewById(R.id.reportButton);

        // Setup RecyclerViews
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ratings RecyclerView
        ratingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ratingList = new ArrayList<>();
        ratingsAdapter = new RatingsAdapter(this, ratingList, recipeId, new RatingsAdapter.OnRatingActionListener() {
            @Override
            public void onRatingDeleted() {
                loadRecipeDetails(); // Refresh recipe stats
            }

            @Override
            public void onRatingEdited(Rating rating) {
                showRateDialog(rating);
            }
        });
        ratingsRecyclerView.setAdapter(ratingsAdapter);

        // Comments RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Post comment button listener
        postCommentButton.setOnClickListener(v -> postComment());
    }

    private void setupCommentAdapter() {
        commentAdapter = new CommentAdapter(this, commentList, new CommentAdapter.OnCommentActionListener() {
            @Override
            public void onDeleteClick(Comment comment, int position) {
                deleteComment(comment, position);
            }

            @Override
            public void onCommentReport(Comment comment) {
                showReportDialog("comment", comment.getCommentId(), comment.getComment());
            }
        });
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void setupRoleBasedUI() {
        chefButtonLayout.setVisibility(View.GONE);
        userButtonLayout.setVisibility(View.GONE);

        if (currentUserRole != null) {
            switch (currentUserRole) {
                case Constants.ROLE_CHEF:
                    chefButtonLayout.setVisibility(View.VISIBLE);
                    setupChefButtons();
                    break;
                case Constants.ROLE_USER:
                    userButtonLayout.setVisibility(View.VISIBLE);
                    setupUserButtons();
                    break;
                case Constants.ROLE_ADMIN:
                    chefButtonLayout.setVisibility(View.VISIBLE);
                    userButtonLayout.setVisibility(View.VISIBLE);
                    setupChefButtons();
                    setupUserButtons();
                    break;
            }
        }
    }

    private void setupChefButtons() {
        editButton.setOnClickListener(v -> editRecipe());
        deleteButton.setOnClickListener(v -> confirmDeleteRecipe());
        statsButton.setOnClickListener(v -> viewStatistics());
    }

    private void setupUserButtons() {
        rateButton.setOnClickListener(v -> rateRecipe());
        likeButton.setOnClickListener(v -> toggleLike());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        shareButton.setOnClickListener(v -> shareRecipe());
        reportButton.setOnClickListener(v -> showReportDialog("recipe", recipeId, currentRecipe.getTitle()));
    }

    private void showReportDialog(String type, String id, String preview) {
        String[] reasons = {"Inappropriate content", "Spam", "Harassment", "False information", "Other"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Report " + type)
                .setItems(reasons, (dialog, which) -> {
                    String reason = reasons[which];
                    reportHelper.reportContent(id, type, reason, preview);
                })
                .show();
    }

    private void loadRecipeDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        currentRecipe = documentSnapshot.toObject(Recipe.class);
                        if (currentRecipe != null) {
                            currentRecipe.setRecipeId(documentSnapshot.getId());
                            displayRecipeDetails();

                            if (Constants.ROLE_CHEF.equals(currentUserRole)) {
                                checkRecipeOwnership();
                            }

                            statisticsHelper.incrementViewCount(recipeId, currentRecipe);
                        }
                    } else {
                        Toast.makeText(this, "Recipe no longer exists", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading recipe", e);
                    Toast.makeText(this, "Error loading recipe", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRatings() {
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .collection(Constants.RATINGS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading ratings", error);
                        return;
                    }

                    if (value != null) {
                        ratingList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Rating rating = doc.toObject(Rating.class);
                            rating.setRatingId(doc.getId());
                            ratingList.add(rating);
                        }
                        ratingsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadComments() {
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .collection(Constants.COMMENTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading comments", error);
                        return;
                    }

                    if (value != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            comment.setCommentId(doc.getId());
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();

                        // Update UI based on comments count
                        int count = commentList.size();
                        commentCountText.setText(count + " Comment" + (count != 1 ? "s" : ""));

                        if (count == 0) {
                            noCommentsText.setVisibility(View.VISIBLE);
                            commentsRecyclerView.setVisibility(View.GONE);
                        } else {
                            noCommentsText.setVisibility(View.GONE);
                            commentsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void postComment() {
        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Constants.USERS_COLLECTION)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String tempUserName = "User";
                    String userImageUrl = null;

                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) tempUserName = name;
                        userImageUrl = documentSnapshot.getString("profileImageUrl");
                    }

                    final String userName = tempUserName;
                    Comment comment = new Comment(recipeId, currentUserId, userName, commentText);
                    comment.setUserImageUrl(userImageUrl);

                    db.collection(Constants.RECIPES_COLLECTION)
                            .document(recipeId)
                            .collection(Constants.COMMENTS_COLLECTION)
                            .add(comment)
                            .addOnSuccessListener(documentReference -> {
                                commentInput.setText("");
                                Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();

                                // Send notification to recipe owner
                                if (currentRecipe != null && !currentUserId.equals(currentRecipe.getChefId())) {
                                    String recipeOwnerId = currentRecipe.getChefId();
                                    String recipeTitle = currentRecipe.getTitle();
                                    String message = userName + " commented on your recipe: " + recipeTitle;

                                    notificationHelper.sendNotification(
                                            recipeOwnerId,
                                            currentUserId,
                                            userName,
                                            Constants.NOTIFICATION_TYPE_COMMENT,
                                            message,
                                            recipeId,
                                            recipeTitle,
                                            null
                                    );

                                    sendPushNotification(recipeOwnerId, message, Constants.NOTIFICATION_TYPE_COMMENT, recipeId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error posting comment", e);
                            });
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
                            Log.d(TAG, "Sending push notification to token: " + fcmToken);
                            Log.d(TAG, "Notification content: " + message);
                        }
                    }
                });
    }

    private void deleteComment(Comment comment, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection(Constants.RECIPES_COLLECTION)
                            .document(recipeId)
                            .collection(Constants.COMMENTS_COLLECTION)
                            .document(comment.getCommentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkUserInteraction() {
        isLiked = false;
        isFavorited = false;
        updateLikeButton();
        updateFavoriteButton();
    }

    private void updateLikeButton() {
        if (likeButton != null) {
            likeButton.setText(isLiked ? "❤️ Liked" : "❤️ Like");
        }
    }

    private void updateFavoriteButton() {
        if (favoriteButton != null) {
            favoriteButton.setText(isFavorited ? "🔖 Saved" : "🔖 Save");
        }
    }

    private void checkRecipeOwnership() {
        if (currentRecipe != null && currentUserId != null) {
            boolean isOwner = currentUserId.equals(currentRecipe.getChefId());
            editButton.setEnabled(isOwner);
            deleteButton.setEnabled(isOwner);
            editButton.setAlpha(isOwner ? 1.0f : 0.5f);
            deleteButton.setAlpha(isOwner ? 1.0f : 0.5f);
        }
    }

    private void displayRecipeDetails() {
        if (currentRecipe == null) return;

        titleText.setText(currentRecipe.getTitle());
        chefText.setText("By " + currentRecipe.getChefName());
        descriptionText.setText(currentRecipe.getDescription());

        String time = "Prep: " + currentRecipe.getPrepTime() + " min  |  Cook: " +
                currentRecipe.getCookTime() + " min";
        timeText.setText(time);
        servingsText.setText("Serves: " + currentRecipe.getServings());

        if (currentRecipe.getAverageRating() > 0) {
            ratingBar.setRating(currentRecipe.getAverageRating());
            ratingCountText.setText("(" + currentRecipe.getTotalRatings() + " ratings)");
        } else {
            ratingCountText.setText("(No ratings yet)");
        }

        if (currentRecipe.getThumbnailUrl() != null && !currentRecipe.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentRecipe.getThumbnailUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(recipeImage);
        }

        if (currentRecipe.getIngredients() != null && !currentRecipe.getIngredients().isEmpty()) {
            noIngredientsText.setVisibility(View.GONE);
            ingredientsRecyclerView.setVisibility(View.VISIBLE);
            ingredientsRecyclerView.setAdapter(new IngredientAdapter(this, currentRecipe.getIngredients()));
        } else {
            noIngredientsText.setVisibility(View.VISIBLE);
            ingredientsRecyclerView.setVisibility(View.GONE);
        }

        if (currentRecipe.getSteps() != null && !currentRecipe.getSteps().isEmpty()) {
            noStepsText.setVisibility(View.GONE);
            stepsRecyclerView.setVisibility(View.VISIBLE);
            stepsRecyclerView.setAdapter(new StepAdapter(this, currentRecipe.getSteps()));
        } else {
            noStepsText.setVisibility(View.VISIBLE);
            stepsRecyclerView.setVisibility(View.GONE);
        }
    }

    private void editRecipe() {
        if (currentRecipe == null || !currentUserId.equals(currentRecipe.getChefId())) return;
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra("recipeId", currentRecipe.getRecipeId());
        startActivity(intent);
    }

    private void confirmDeleteRecipe() {
        if (currentRecipe == null || !currentUserId.equals(currentRecipe.getChefId())) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRecipe())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRecipe() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                });
    }

    private void viewStatistics() {
        if (currentRecipe == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String stats = "👁️ Views: " + currentRecipe.getViews() + "\n" +
                "❤️ Likes: " + currentRecipe.getLikes() + "\n" +
                "📤 Shares: " + currentRecipe.getShares() + "\n" +
                "🔖 Saves: " + currentRecipe.getSaves() + "\n" +
                "⭐ Rating: " + String.format("%.1f", currentRecipe.getAverageRating()) +
                " (" + currentRecipe.getTotalRatings() + " reviews)";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Recipe Statistics")
                .setMessage(stats)
                .setPositiveButton("OK", null)
                .show();
    }

    private void rateRecipe() {
        showRateDialog(null);
    }

    private void showRateDialog(Rating existingRating) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_recipe, null);
        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        TextInputEditText reviewInput = dialogView.findViewById(R.id.reviewInput);

        if (existingRating != null) {
            dialogRatingBar.setRating(existingRating.getRating());
            reviewInput.setText(existingRating.getReview());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(existingRating == null ? "Rate Recipe" : "Edit Review")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float ratingValue = dialogRatingBar.getRating();
                    String review = reviewInput.getText().toString().trim();
                    if (ratingValue > 0) {
                        submitRating(ratingValue, review, existingRating);
                    } else {
                        Toast.makeText(this, "Please select at least 1 star", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRating(float ratingValue, String review, Rating existingRating) {
        progressBar.setVisibility(View.VISIBLE);

        Rating rating = existingRating != null ? existingRating : new Rating();
        rating.setRating(ratingValue);
        rating.setReview(review);
        rating.setUserId(currentUserId);
        rating.setUserName(sessionManager.getUserName());
        rating.setCreatedAt(System.currentTimeMillis());

        if (existingRating == null) {
            db.collection(Constants.RECIPES_COLLECTION)
                    .document(recipeId)
                    .collection(Constants.RATINGS_COLLECTION)
                    .add(rating)
                    .addOnSuccessListener(doc -> {
                        updateRecipeRatingStats(ratingValue, true);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection(Constants.RECIPES_COLLECTION)
                    .document(recipeId)
                    .collection(Constants.RATINGS_COLLECTION)
                    .document(existingRating.getRatingId())
                    .set(rating)
                    .addOnSuccessListener(aVoid -> {
                        updateRecipeRatingStats(ratingValue, false);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update rating", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateRecipeRatingStats(float newRating, boolean isNew) {
        // Recalculate average rating
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .collection(Constants.RATINGS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float total = 0;
                    int count = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Rating r = doc.toObject(Rating.class);
                        total += r.getRating();
                    }

                    float average = count > 0 ? total / count : 0;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("averageRating", average);
                    updates.put("totalRatings", count);

                    db.collection(Constants.RECIPES_COLLECTION)
                            .document(recipeId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                if (currentRecipe != null) {
                                    currentRecipe.setAverageRating(average);
                                    currentRecipe.setTotalRatings(count);
                                    ratingBar.setRating(average);
                                    ratingCountText.setText("(" + count + " ratings)");
                                }
                                Toast.makeText(this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void toggleLike() {
        if (currentRecipe == null) return;
        if (isLiked) {
            statisticsHelper.decrementLikeCount(recipeId, currentRecipe);
            isLiked = false;
        } else {
            statisticsHelper.incrementLikeCount(recipeId, currentRecipe);
            isLiked = true;

            // Send notification to recipe owner
            if (!currentUserId.equals(currentRecipe.getChefId())) {
                String recipeOwnerId = currentRecipe.getChefId();
                String recipeTitle = currentRecipe.getTitle();
                String currentUserName = sessionManager.getUserName();
                String message = currentUserName + " liked your recipe: " + recipeTitle;

                notificationHelper.sendNotification(
                        recipeOwnerId,
                        currentUserId,
                        currentUserName,
                        Constants.NOTIFICATION_TYPE_LIKE,
                        message,
                        recipeId,
                        recipeTitle,
                        null
                );

                sendPushNotification(recipeOwnerId, message, Constants.NOTIFICATION_TYPE_LIKE, recipeId);
            }
        }
        updateLikeButton();
    }

    private void toggleFavorite() {
        if (currentRecipe == null) return;
        if (isFavorited) {
            statisticsHelper.decrementSaveCount(recipeId, currentRecipe);
            isFavorited = false;
        } else {
            statisticsHelper.incrementSaveCount(recipeId, currentRecipe);
            isFavorited = true;
        }
        updateFavoriteButton();
    }

    private void shareRecipe() {
        if (currentRecipe == null) return;
        statisticsHelper.incrementShareCount(recipeId, currentRecipe);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Check out this recipe: " + currentRecipe.getTitle() +
                " on RecipeMaster Pro!");
        startActivity(Intent.createChooser(intent, "Share via"));
    }
}
