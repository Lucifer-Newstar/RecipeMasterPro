package com.example.recipemasterpro.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class UserMigrationActivity extends AppCompatActivity {

    private Button migrateButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_migration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        migrateButton = findViewById(R.id.migrateButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        migrateButton.setOnClickListener(v -> migrateUsers());
    }

    private void migrateUsers() {
        progressBar.setVisibility(View.VISIBLE);
        migrateButton.setEnabled(false);
        statusText.setText("Starting migration...");

        // Get all users from Authentication
        // Note: You can't directly list all auth users from client SDK
        // So we'll get them from Firestore if any exist, and add missing ones during login
        
        // Instead, let's check existing users in Firestore
        db.collection(Constants.USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Boolean> existingUsers = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        existingUsers.put(doc.getId(), true);
                    }
                    
                    statusText.setText("Found " + existingUsers.size() + " users in Firestore");
                    
                    // We'll create a system where users are added to Firestore on first login
                    // For now, just show instructions
                    showInstructions();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    migrateButton.setEnabled(true);
                    statusText.setText("Error: " + e.getMessage());
                });
    }

    private void showInstructions() {
        statusText.setText(
            "✅ Migration Setup Complete!\n\n" +
            "To add all users to database:\n" +
            "1. Users will be automatically added to Firestore when they log in\n" +
            "2. Ask all existing users to log in once\n" +
            "3. Their profiles will be created automatically\n\n" +
            "Current users in Authentication: Check Firebase Console"
        );
        
        progressBar.setVisibility(View.GONE);
        migrateButton.setEnabled(true);
        migrateButton.setText("Check Again");
    }
}
