package com.example.recipemasterpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.admin.ContentAdminAdapter;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.models.Reel;
import com.example.recipemasterpro.utils.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ContentModerationFragment extends Fragment {

    private static final String ARG_TYPE = "content_type";

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private ContentAdminAdapter adapter;
    private List<Object> contentList;
    private FirebaseFirestore db;

    private String currentTab = "recipes";

    public static ContentModerationFragment newInstance(String type) {
        ContentModerationFragment fragment = new ContentModerationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_moderation, container, false);

        db = FirebaseFirestore.getInstance();
        contentList = new ArrayList<>();

        if (getArguments() != null) {
            currentTab = getArguments().getString(ARG_TYPE, "recipes");
        }

        initViews(view);
        setupTabLayout();
        loadContent(currentTab);

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupTabLayout() {
        // Set initial tab based on currentTab
        if ("reels".equals(currentTab)) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadContent("recipes");
                } else {
                    loadContent("reels");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadContent(String type) {
        currentTab = type;
        progressBar.setVisibility(View.VISIBLE);
        contentList.clear();

        if ("recipes".equals(type)) {
            db.collection(Constants.RECIPES_COLLECTION)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Recipe recipe = doc.toObject(Recipe.class);
                            recipe.setRecipeId(doc.getId());
                            contentList.add(recipe);
                        }
                        setupAdapter();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading recipes", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection(Constants.REELS_COLLECTION)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Reel reel = doc.toObject(Reel.class);
                            reel.setReelId(doc.getId());
                            contentList.add(reel);
                        }
                        setupAdapter();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading reels", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupAdapter() {
        adapter = new ContentAdminAdapter(getContext(), contentList, currentTab, new ContentAdminAdapter.OnContentActionListener() {
            @Override
            public void onContentClick(Object content) {
                showContentDetails(content);
            }

            @Override
            public void onContentDelete(Object content) {
                confirmDelete(content);
            }
        });

        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);

        if (contentList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showContentDetails(Object content) {
        String title = "";
        String details = "";
        
        if (content instanceof Recipe) {
            Recipe recipe = (Recipe) content;
            title = recipe.getTitle();
            details = "Chef: " + recipe.getChefName() + "\n" +
                      "Category: " + recipe.getCategory() + "\n" +
                      "Cuisine: " + recipe.getCuisine() + "\n" +
                      "Views: " + recipe.getViews() + "\n" +
                      "Likes: " + recipe.getLikes();
        } else if (content instanceof Reel) {
            Reel reel = (Reel) content;
            title = reel.getTitle();
            details = "Chef: " + reel.getChefName() + "\n" +
                      "Views: " + reel.getViews() + "\n" +
                      "Likes: " + reel.getLikes();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmDelete(Object content) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Content")
                .setMessage("Are you sure you want to permanently delete this content? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteContent(content);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteContent(Object content) {
        if (content instanceof Recipe) {
            Recipe recipe = (Recipe) content;
            db.collection(Constants.RECIPES_COLLECTION)
                    .document(recipe.getRecipeId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                        loadContent(currentTab);
                    });
        } else if (content instanceof Reel) {
            Reel reel = (Reel) content;
            db.collection(Constants.REELS_COLLECTION)
                    .document(reel.getReelId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Reel deleted successfully", Toast.LENGTH_SHORT).show();
                        loadContent(currentTab);
                    });
        }
    }
}
