package com.example.recipemasterpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class AnalyticsFragment extends Fragment {

    private TextView totalUsersText, totalRecipesText, totalReelsText;
    private TextView totalViewsText, totalLikesText, totalCommentsText;
    private TextView activeUsersText, chefsCountText;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        loadAnalytics();

        return view;
    }

    private void initViews(View view) {
        totalUsersText = view.findViewById(R.id.totalUsersText);
        totalRecipesText = view.findViewById(R.id.totalRecipesText);
        totalReelsText = view.findViewById(R.id.totalReelsText);
        totalViewsText = view.findViewById(R.id.totalViewsText);
        totalLikesText = view.findViewById(R.id.totalLikesText);
        totalCommentsText = view.findViewById(R.id.totalCommentsText);
        activeUsersText = view.findViewById(R.id.activeUsersText);
        chefsCountText = view.findViewById(R.id.chefsCountText);
    }

    private void loadAnalytics() {
        // Get total users
        db.collection(Constants.USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    totalUsersText.setText(String.valueOf(totalUsers));

                    // Count chefs
                    int chefs = 0;
                    for (var doc : queryDocumentSnapshots) {
                        String role = doc.getString("role");
                        if (Constants.ROLE_CHEF.equals(role)) {
                            chefs++;
                        }
                    }
                    chefsCountText.setText(String.valueOf(chefs));
                });

        // Get total recipes and stats
        db.collection(Constants.RECIPES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalRecipes = queryDocumentSnapshots.size();
                    totalRecipesText.setText(String.valueOf(totalRecipes));

                    int totalViews = 0;
                    int totalLikes = 0;

                    for (var doc : queryDocumentSnapshots) {
                        Long views = doc.getLong("views");
                        Long likes = doc.getLong("likes");

                        if (views != null) totalViews += views;
                        if (likes != null) totalLikes += likes;
                    }

                    totalViewsText.setText(String.valueOf(totalViews));
                    totalLikesText.setText(String.valueOf(totalLikes));
                });

        // Get total reels
        db.collection(Constants.REELS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalReelsText.setText(String.valueOf(queryDocumentSnapshots.size()));
                });

        // Get total comments across all recipes (simplified)
        // You might need a separate collection for comments count
        totalCommentsText.setText("N/A");

        // Active users (last 24h) - simplified
        activeUsersText.setText("N/A");
    }
}