package com.example.recipemasterpro.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.EditableIngredientAdapter;
import com.example.recipemasterpro.adapters.EditableStepAdapter;
import com.example.recipemasterpro.models.Ingredient;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.models.Step;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditRecipeActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText titleEditText, descriptionEditText, prepTimeEditText, cookTimeEditText, servingsEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;

    // Ingredients section
    private LinearLayout addIngredientSection;
    private RecyclerView ingredientsRecyclerView;
    private MaterialButton addIngredientButton;
    private EditableIngredientAdapter ingredientAdapter;
    private List<Ingredient> ingredientsList;

    // Steps section
    private LinearLayout addStepSection;
    private RecyclerView stepsRecyclerView;
    private MaterialButton addStepButton;
    private EditableStepAdapter stepAdapter;
    private List<Step> stepsList;

    // Firebase
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    // Data
    private String recipeId;
    private Recipe currentRecipe;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        currentUserId = sessionManager.getUserId();

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipeId");

        initViews();
        setupClickListeners();

        if (recipeId != null) {
            loadRecipeDetails();
        } else {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
        }
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

        // Ingredients section
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        ingredientsList = new ArrayList<>();
        ingredientAdapter = new EditableIngredientAdapter(this, ingredientsList);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setAdapter(ingredientAdapter);

        // Steps section
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        addStepButton = findViewById(R.id.addStepButton);
        stepsList = new ArrayList<>();
        stepAdapter = new EditableStepAdapter(this, stepsList);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(stepAdapter);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipeChanges();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    private void loadRecipeDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    currentRecipe = documentSnapshot.toObject(Recipe.class);
                    if (currentRecipe != null) {
                        currentRecipe.setRecipeId(documentSnapshot.getId());

                        // Check if this user owns the recipe
                        if (!currentUserId.equals(currentRecipe.getChefId())) {
                            Toast.makeText(this, "You don't have permission to edit this recipe",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        displayRecipeDetails();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading recipe: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayRecipeDetails() {
        // Set basic info
        titleEditText.setText(currentRecipe.getTitle());
        descriptionEditText.setText(currentRecipe.getDescription());

        if (currentRecipe.getPrepTime() > 0) {
            prepTimeEditText.setText(String.valueOf(currentRecipe.getPrepTime()));
        }

        if (currentRecipe.getCookTime() > 0) {
            cookTimeEditText.setText(String.valueOf(currentRecipe.getCookTime()));
        }

        if (currentRecipe.getServings() > 0) {
            servingsEditText.setText(String.valueOf(currentRecipe.getServings()));
        }

        // Load ingredients
        if (currentRecipe.getIngredients() != null) {
            ingredientsList.clear();
            ingredientsList.addAll(currentRecipe.getIngredients());
            ingredientAdapter.notifyDataSetChanged();
        }

        // Load steps
        if (currentRecipe.getSteps() != null) {
            stepsList.clear();
            stepsList.addAll(currentRecipe.getSteps());
            stepAdapter.notifyDataSetChanged();
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

    private void saveRecipeChanges() {
        // Validate inputs
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

        // Update recipe object
        currentRecipe.setTitle(title);
        currentRecipe.setDescription(description);

        String prepTimeStr = prepTimeEditText.getText().toString().trim();
        String cookTimeStr = cookTimeEditText.getText().toString().trim();
        String servingsStr = servingsEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(prepTimeStr)) {
            currentRecipe.setPrepTime(Integer.parseInt(prepTimeStr));
        }

        if (!TextUtils.isEmpty(cookTimeStr)) {
            currentRecipe.setCookTime(Integer.parseInt(cookTimeStr));
        }

        if (!TextUtils.isEmpty(servingsStr)) {
            currentRecipe.setServings(Integer.parseInt(servingsStr));
        }

        // Update ingredients and steps
        currentRecipe.setIngredients(ingredientsList);
        currentRecipe.setSteps(stepsList);
        currentRecipe.setUpdatedAt(System.currentTimeMillis());

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .set(currentRecipe)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Recipe updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Error updating recipe: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}