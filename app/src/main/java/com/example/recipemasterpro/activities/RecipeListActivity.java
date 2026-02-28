package com.example.recipemasterpro.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.RecipeAdapter;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        db = FirebaseFirestore.getInstance();
        recipeList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadRecipes();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        setTitle("Browse Recipes");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, recipeList);
        recyclerView.setAdapter(adapter);
    }

    private void loadRecipes() {
        db.collection(Constants.RECIPES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipeList.add(recipe);
                    }
                    adapter.notifyDataSetChanged();

                    if (recipeList.isEmpty()) {
                        Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}