package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.RecipeAdapter;
import com.example.recipemasterpro.adapters.SearchSuggestionAdapter;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SearchHistoryManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ImageView clearButton, backButton;
    private RecyclerView resultsRecyclerView, suggestionsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyResultsText, recentTitleText;
    private View suggestionsContainer;

    private FirebaseFirestore db;
    private RecipeAdapter recipeAdapter;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<Recipe> recipeList;
    private List<String> suggestionList;
    private SearchHistoryManager searchHistoryManager;

    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();
        searchHistoryManager = new SearchHistoryManager(this);
        recipeList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        initViews();
        setupAdapters();
        setupListeners();
        loadRecentSearches();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        clearButton = findViewById(R.id.clearButton);
        backButton = findViewById(R.id.backButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyResultsText = findViewById(R.id.emptyResultsText);
        recentTitleText = findViewById(R.id.recentTitleText);
        suggestionsContainer = findViewById(R.id.suggestionsContainer);

        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapters() {
        recipeAdapter = new RecipeAdapter(this, recipeList, recipe -> {
            // Open recipe detail
            startActivity(new Intent(this, RecipeDetailActivity.class)
                    .putExtra("recipeId", recipe.getRecipeId()));
        });

        suggestionAdapter = new SearchSuggestionAdapter(this, suggestionList, suggestion -> {
            searchEditText.setText(suggestion);
            performSearch(suggestion);
            searchHistoryManager.addSearch(suggestion);
        });

        resultsRecyclerView.setAdapter(recipeAdapter);
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            showSuggestions();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearButton.setVisibility(View.VISIBLE);
                    // Debounce search
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    searchRunnable = () -> performSearch(s.toString());
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    clearButton.setVisibility(View.GONE);
                    showSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            showSuggestions();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.GONE);
        suggestionsContainer.setVisibility(View.GONE);
        emptyResultsText.setVisibility(View.GONE);

        // Search in multiple fields
        db.collection(Constants.RECIPES_COLLECTION)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        recipe.setRecipeId(doc.getId());
                        recipeList.add(recipe);
                    }

                    // Also search by chef name
                    searchByChefName(query);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void searchByChefName(String query) {
        db.collection(Constants.RECIPES_COLLECTION)
                .whereGreaterThanOrEqualTo("chefName", query)
                .whereLessThanOrEqualTo("chefName", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        recipe.setRecipeId(doc.getId());
                        if (!recipeList.contains(recipe)) {
                            recipeList.add(recipe);
                        }
                    }
                    displayResults();
                })
                .addOnFailureListener(e -> {
                    displayResults();
                });
    }

    private void searchByIngredients(String query) {
        // Complex search - can be implemented later
        // For now, just show results from title and chef search
        displayResults();
    }

    private void displayResults() {
        progressBar.setVisibility(View.GONE);

        if (recipeList.isEmpty()) {
            emptyResultsText.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
            suggestionsContainer.setVisibility(View.GONE);
        } else {
            emptyResultsText.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);
            suggestionsContainer.setVisibility(View.GONE);
            recipeAdapter.notifyDataSetChanged();
        }
    }

    private void showSuggestions() {
        resultsRecyclerView.setVisibility(View.GONE);
        suggestionsContainer.setVisibility(View.VISIBLE);
        emptyResultsText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        loadRecentSearches();
    }

    private void loadRecentSearches() {
        List<String> recent = searchHistoryManager.getRecentSearches();
        suggestionList.clear();
        suggestionList.addAll(recent);

        if (suggestionList.isEmpty()) {
            recentTitleText.setText("Popular Searches");
            // Load popular searches from Firebase
            loadPopularSearches();
        } else {
            recentTitleText.setText("Recent Searches");
            suggestionAdapter.notifyDataSetChanged();
        }
    }

    private void loadPopularSearches() {
        // You can implement popular searches based on most searched terms
        // For now, add some default suggestions
        suggestionList.add("Pasta");
        suggestionList.add("Chicken");
        suggestionList.add("Vegetarian");
        suggestionList.add("Dessert");
        suggestionList.add("Quick & Easy");
        suggestionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}