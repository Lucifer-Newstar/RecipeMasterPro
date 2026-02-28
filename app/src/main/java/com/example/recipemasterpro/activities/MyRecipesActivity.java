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
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        recipeList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadMyRecipes();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        setTitle("My Recipes");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, recipeList);
        recyclerView.setAdapter(adapter);
    }

    private void loadMyRecipes() {
        String chefId = sessionManager.getUserId();

        db.collection(Constants.RECIPES_COLLECTION)
                .whereEqualTo("chefId", chefId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
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
                        Toast.makeText(this, "You haven't created any recipes yet", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}