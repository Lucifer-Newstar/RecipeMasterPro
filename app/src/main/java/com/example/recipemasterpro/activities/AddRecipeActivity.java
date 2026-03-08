package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.recipemasterpro.adapters.EditableIngredientAdapter;
import com.example.recipemasterpro.adapters.EditableStepAdapter;
import com.example.recipemasterpro.models.Ingredient;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.models.Step;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.NotificationHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.example.recipemasterpro.utils.VideoUploadHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private static final String TAG = "AddRecipeActivity";
    private static final int PICK_VIDEO_REQUEST = 100;
    private static final int PICK_REEL_REQUEST = 101;

    // UI Components
    private TextInputEditText titleEditText, descriptionEditText, prepTimeEditText, cookTimeEditText, servingsEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;

    // Video sections
    private LinearLayout fullVideoSection, reelVideoSection;
    private Button selectFullVideoButton, selectReelButton;
    private TextView fullVideoNameText, reelVideoNameText;
    private ImageView fullVideoPreview, reelVideoPreview;
    private ProgressBar fullVideoProgress, reelVideoProgress;

    // Ingredients section
    private RecyclerView ingredientsRecyclerView;
    private MaterialButton addIngredientButton;
    private EditableIngredientAdapter ingredientAdapter;
    private List<Ingredient> ingredientsList;

    // Steps section
    private RecyclerView stepsRecyclerView;
    private MaterialButton addStepButton;
    private EditableStepAdapter stepAdapter;
    private List<Step> stepsList;

    // Firebase
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;
    private VideoUploadHelper videoUploadHelper;
    private NotificationHelper notificationHelper;

    // Video URIs
    private Uri fullVideoUri;
    private Uri reelVideoUri;

    // Activity result launchers
    private ActivityResultLauncher<Intent> fullVideoPickerLauncher;
    private ActivityResultLauncher<Intent> reelVideoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        videoUploadHelper = new VideoUploadHelper(this);
        notificationHelper = new NotificationHelper();

        // Initialize lists
        ingredientsList = new ArrayList<>();
        stepsList = new ArrayList<>();

        initViews();
        setupVideoPickers();
        setupClickListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        prepTimeEditText = findViewById(R.id.prepTimeEditText);
        cookTimeEditText = findViewById(R.id.cookTimeEditText);
        servingsEditText = findViewById(R.id.servingsEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);

        // Video sections
        fullVideoSection = findViewById(R.id.fullVideoSection);
        reelVideoSection = findViewById(R.id.reelVideoSection);
        selectFullVideoButton = findViewById(R.id.selectFullVideoButton);
        selectReelButton = findViewById(R.id.selectReelButton);
        fullVideoNameText = findViewById(R.id.fullVideoNameText);
        reelVideoNameText = findViewById(R.id.reelVideoNameText);
        fullVideoPreview = findViewById(R.id.fullVideoPreview);
        reelVideoPreview = findViewById(R.id.reelVideoPreview);
        fullVideoProgress = findViewById(R.id.fullVideoProgress);
        reelVideoProgress = findViewById(R.id.reelVideoProgress);

        // Ingredients section
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientAdapter = new EditableIngredientAdapter(this, ingredientsList);
        ingredientsRecyclerView.setAdapter(ingredientAdapter);

        // Steps section
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        addStepButton = findViewById(R.id.addStepButton);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new EditableStepAdapter(this, stepsList);
        stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void setupVideoPickers() {
        fullVideoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            fullVideoUri = result.getData().getData();
                            fullVideoNameText.setText(fullVideoUri.getLastPathSegment());
                            fullVideoPreview.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                });

        reelVideoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            reelVideoUri = result.getData().getData();
                            reelVideoNameText.setText(reelVideoUri.getLastPathSegment());
                            reelVideoPreview.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                });
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectFullVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoPicker(PICK_VIDEO_REQUEST);
            }
        });

        selectReelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoPicker(PICK_REEL_REQUEST);
            }
        });

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddIngredientDialog();
            }
        });

        addStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStepDialog();
            }
        });
    }

    private void openVideoPicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");

        if (requestCode == PICK_VIDEO_REQUEST) {
            fullVideoPickerLauncher.launch(intent);
        } else {
            reelVideoPickerLauncher.launch(intent);
        }
    }

    private void showAddIngredientDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_ingredient, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.ingredientNameInput);
        TextInputEditText quantityInput = dialogView.findViewById(R.id.ingredientQuantityInput);
        TextInputEditText unitInput = dialogView.findViewById(R.id.ingredientUnitInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Ingredient")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String quantity = quantityInput.getText().toString().trim();
                    String unit = unitInput.getText().toString().trim();

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(quantity)) {
                        Ingredient ingredient = new Ingredient(name, quantity, unit);
                        ingredientsList.add(ingredient);
                        ingredientAdapter.notifyItemInserted(ingredientsList.size() - 1);
                    } else {
                        Toast.makeText(this, "Name and quantity are required",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddStepDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_step, null);
        TextInputEditText instructionInput = dialogView.findViewById(R.id.stepInstructionInput);
        TextInputEditText durationInput = dialogView.findViewById(R.id.stepDurationInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Step")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String instruction = instructionInput.getText().toString().trim();
                    String durationStr = durationInput.getText().toString().trim();

                    if (!TextUtils.isEmpty(instruction)) {
                        Step step = new Step();
                        step.setStepNumber(stepsList.size() + 1);
                        step.setInstruction(instruction);

                        if (!TextUtils.isEmpty(durationStr)) {
                            step.setDuration(Integer.parseInt(durationStr));
                        }

                        stepsList.add(step);
                        stepAdapter.notifyItemInserted(stepsList.size() - 1);
                    } else {
                        Toast.makeText(this, "Instruction is required",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveRecipe() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Description is required");
            return;
        }

        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName(); // Better to use name than email

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // First upload videos if selected
        if (fullVideoUri != null || reelVideoUri != null) {
            uploadVideosAndSaveRecipe(title, description, userId, userName);
        } else {
            saveRecipeToFirestore(title, description, userId, userName, null, null);
        }
    }

    private void uploadVideosAndSaveRecipe(String title, String description, String userId, String userName) {
        final String[] fullVideoUrl = {null};
        final String[] reelVideoUrl = {null};
        final int[] uploadsCompleted = {0};
        final int totalUploads = (fullVideoUri != null ? 1 : 0) + (reelVideoUri != null ? 1 : 0);

        VideoUploadHelper.VideoUploadCallback callback = new VideoUploadHelper.VideoUploadCallback() {
            @Override
            public void onUploadSuccess(String videoUrl) {
                // Determine which video was just uploaded based on which ones are still null
                if (fullVideoUri != null && fullVideoUrl[0] == null) {
                    fullVideoUrl[0] = videoUrl;
                } else {
                    reelVideoUrl[0] = videoUrl;
                }

                uploadsCompleted[0]++;
                if (uploadsCompleted[0] == totalUploads) {
                    saveRecipeToFirestore(title, description, userId, userName,
                            fullVideoUrl[0], reelVideoUrl[0]);
                }
            }

            @Override
            public void onUploadFailure(String error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(AddRecipeActivity.this,
                        "Video upload failed: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(int progress) {
                // Update progress bars
                if (fullVideoUri != null && fullVideoUrl[0] == null) {
                    fullVideoProgress.setProgress(progress);
                    fullVideoProgress.setVisibility(View.VISIBLE);
                } else if (reelVideoUri != null) {
                    reelVideoProgress.setProgress(progress);
                    reelVideoProgress.setVisibility(View.VISIBLE);
                }
            }
        };

        if (fullVideoUri != null) {
            videoUploadHelper.uploadVideo(fullVideoUri, "full_videos", callback);
        }

        if (reelVideoUri != null) {
            videoUploadHelper.uploadVideo(reelVideoUri, "reel_videos", callback);
        }
    }

    private void saveRecipeToFirestore(String title, String description, String userId,
                                       String userName, String fullVideoUrl, String reelVideoUrl) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setChefId(userId);
        recipe.setChefName(userName != null ? userName : "Unknown Chef");
        recipe.setPrepTime(TextUtils.isEmpty(prepTimeEditText.getText()) ? 0 :
                Integer.parseInt(prepTimeEditText.getText().toString()));
        recipe.setCookTime(TextUtils.isEmpty(cookTimeEditText.getText()) ? 0 :
                Integer.parseInt(cookTimeEditText.getText().toString()));
        recipe.setServings(TextUtils.isEmpty(servingsEditText.getText()) ? 1 :
                Integer.parseInt(servingsEditText.getText().toString()));
        recipe.setIngredients(ingredientsList);
        recipe.setSteps(stepsList);
        recipe.setFullVideoUrl(fullVideoUrl);
        recipe.setReelVideoUrl(reelVideoUrl);
        recipe.setCreatedAt(System.currentTimeMillis());
        recipe.setUpdatedAt(System.currentTimeMillis());

        db.collection(Constants.RECIPES_COLLECTION)
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);

                    // Notify followers about new recipe
                    notifyFollowers(userId, recipe.getChefName(), documentReference.getId(), title);

                    Toast.makeText(AddRecipeActivity.this,
                            "Recipe saved successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(AddRecipeActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving recipe", e);
                });
    }

    private void notifyFollowers(String chefId, String chefName, String recipeId, String recipeTitle) {
        // Get all followers and send notifications
        db.collection(Constants.USERS_COLLECTION)
                .document(chefId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User chef = documentSnapshot.toObject(User.class);
                    if (chef != null && chef.getFollowers() != null) {
                        for (String followerId : chef.getFollowers()) {
                            String message = chefName + " shared a new recipe: " + recipeTitle;

                            notificationHelper.sendNotification(
                                    followerId,
                                    chefId,
                                    chefName,
                                    Constants.NOTIFICATION_TYPE_NEW_RECIPE,
                                    message,
                                    recipeId,
                                    recipeTitle,
                                    null
                            );

                            sendPushNotification(followerId, message,
                                    Constants.NOTIFICATION_TYPE_NEW_RECIPE, recipeId);
                        }
                    }
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
}