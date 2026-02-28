package com.example.recipemasterpro.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, prepTimeEditText, cookTimeEditText, servingsEditText;
    private Button saveButton, addIngredientButton, addStepButton;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        initViews();
        initRecipe();
        setupClickListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        prepTimeEditText = findViewById(R.id.prepTimeEditText);
        cookTimeEditText = findViewById(R.id.cookTimeEditText);
        servingsEditText = findViewById(R.id.servingsEditText);
        saveButton = findViewById(R.id.saveButton);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        addStepButton = findViewById(R.id.addStepButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initRecipe() {
        currentRecipe = new Recipe();
        currentRecipe.setChefId(sessionManager.getUserId());
        currentRecipe.setChefName("Chef Name"); // You might want to fetch this from user data
        currentRecipe.setIngredients(new ArrayList<>());
        currentRecipe.setSteps(new ArrayList<>());
        currentRecipe.setCreatedAt(System.currentTimeMillis());
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddRecipeActivity.this, "Add ingredient feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        addStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddRecipeActivity.this, "Add step feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecipe() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String prepTimeStr = prepTimeEditText.getText().toString().trim();
        String cookTimeStr = cookTimeEditText.getText().toString().trim();
        String servingsStr = servingsEditText.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // Set recipe data
        currentRecipe.setTitle(title);
        currentRecipe.setDescription(description);

        if (!prepTimeStr.isEmpty()) {
            currentRecipe.setPrepTime(Integer.parseInt(prepTimeStr));
        }

        if (!cookTimeStr.isEmpty()) {
            currentRecipe.setCookTime(Integer.parseInt(cookTimeStr));
        }

        if (!servingsStr.isEmpty()) {
            currentRecipe.setServings(Integer.parseInt(servingsStr));
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Save to Firestore
        db.collection(Constants.RECIPES_COLLECTION)
                .add(currentRecipe)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(AddRecipeActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(AddRecipeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}