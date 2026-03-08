package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.NotificationAdapter;
import com.example.recipemasterpro.models.Notification;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.NotificationHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        notificationHelper = new NotificationHelper();
        notificationList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        setTitle("Notifications");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList, notification -> {
            // Handle notification click
            notificationHelper.markAsRead(notification.getNotificationId());

            if ("follow".equals(notification.getType())) {
                // Open profile
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("userId", notification.getTriggerUserId());
                startActivity(intent);
            } else if (notification.getRecipeId() != null) {
                // Open recipe
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("recipeId", notification.getRecipeId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        String userId = sessionManager.getUserId();
        if (userId == null) {
            progressBar.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Please login to see notifications");
            return;
        }

        db.collection(Constants.NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        notification.setNotificationId(doc.getId());
                        notificationList.add(notification);
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load notifications");
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}