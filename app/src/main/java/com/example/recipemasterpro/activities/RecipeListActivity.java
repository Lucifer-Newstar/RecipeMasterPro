package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        
        // Optimize RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        // Create adapter with click listener
        adapter = new RecipeAdapter(this, recipeList, new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                // Open RecipeDetailActivity when recipe is clicked
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                intent.putExtra("recipeTitle", recipe.getTitle());
                startActivity(intent);
            }
        });
        
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
                    } else {
                        Toast.makeText(this, "Found " + recipeList.size() + " recipes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}