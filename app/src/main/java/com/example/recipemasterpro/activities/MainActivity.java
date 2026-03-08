package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.activities.admin.AdminActivity;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.NotificationHelper;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private NotificationHelper notificationHelper;
    private TextView welcomeText, notificationBadgeCount;
    private Button actionButton1, actionButton2, actionButton3, logoutButton, adminButton;
    private RelativeLayout notificationIcon;
    private FirebaseAuth mAuth;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = SessionManager.getInstance(this);
        notificationHelper = new NotificationHelper();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRoleBasedUI();
        setupClickListeners();
        loadUnreadCount();
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        actionButton1 = findViewById(R.id.actionButton1);
        actionButton2 = findViewById(R.id.actionButton2);
        actionButton3 = findViewById(R.id.actionButton3);
        logoutButton = findViewById(R.id.logoutButton);
        adminButton = findViewById(R.id.adminButton);
        notificationIcon = findViewById(R.id.notificationIcon);
        notificationBadgeCount = findViewById(R.id.notificationBadge);
    }

    private void setupRoleBasedUI() {
        userRole = sessionManager.getUserRole();

        if (userRole != null) {
            switch (userRole) {
                case Constants.ROLE_ADMIN:
                    welcomeText.setText("Welcome Admin!");
                    actionButton1.setText("👥 Manage Users");
                    actionButton2.setText("📋 Manage Recipes");
                    actionButton3.setText("📊 View Analytics");
                    adminButton.setVisibility(View.VISIBLE);
                    break;

                case Constants.ROLE_CHEF:
                    welcomeText.setText("Welcome Chef! 👨‍🍳");
                    actionButton1.setText("➕ Add New Recipe");
                    actionButton2.setText("📚 My Recipes");
                    actionButton3.setText("📈 Statistics");
                    adminButton.setVisibility(View.GONE);
                    break;

                case Constants.ROLE_USER:
                    welcomeText.setText("Welcome Food Lover! 🍳");
                    actionButton1.setText("🔍 Browse Recipes");
                    actionButton2.setText("❤️ My Favorites");
                    actionButton3.setText("🛒 Shopping List");
                    adminButton.setVisibility(View.GONE);
                    
                    // Add search button if it exists
                    Button searchButton = findViewById(R.id.searchButton);
                    if (searchButton != null) {
                        searchButton.setVisibility(View.VISIBLE);
                        searchButton.setOnClickListener(v -> {
                            // Assuming SearchActivity exists in the same package
                            try {
                                Class<?> searchActivityClass = Class.forName("com.example.recipemasterpro.activities.SearchActivity");
                                startActivity(new Intent(MainActivity.this, searchActivityClass));
                            } catch (ClassNotFoundException e) {
                                Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;

                default:
                    welcomeText.setText("Welcome!");
                    adminButton.setVisibility(View.GONE);
                    break;
            }
        } else {
            adminButton.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        actionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButton1Click();
            }
        });

        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButton2Click();
            }
        });

        actionButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButton3Click();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        if (adminButton != null) {
            adminButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            });
        }

        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                // Assuming NotificationActivity exists in the same package
                try {
                    Class<?> notificationActivityClass = Class.forName("com.example.recipemasterpro.activities.NotificationActivity");
                    startActivity(new Intent(MainActivity.this, notificationActivityClass));
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUnreadCount() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        notificationHelper.getUnreadCount(userId, count -> {
            if (notificationBadgeCount != null) {
                if (count > 0) {
                    notificationBadgeCount.setText(String.valueOf(count));
                    notificationBadgeCount.setVisibility(View.VISIBLE);
                } else {
                    notificationBadgeCount.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadCount();
    }

    private void handleButton1Click() {
        if (userRole == null) return;
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: Add New Recipe
                startActivityByName("com.example.recipemasterpro.activities.AddRecipeActivity");
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: Browse Recipes
                startActivityByName("com.example.recipemasterpro.activities.RecipeListActivity");
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: Manage Users
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleButton2Click() {
        if (userRole == null) return;
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: My Recipes
                startActivityByName("com.example.recipemasterpro.activities.MyRecipesActivity");
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: My Favorites
                Toast.makeText(this, "❤️ Favorites feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: Manage Recipes
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleButton3Click() {
        if (userRole == null) return;
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: Recipe Statistics
                Toast.makeText(this, "📈 Statistics feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: Shopping List
                Toast.makeText(this, "🛒 Shopping List feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: View Analytics
                startActivity(new Intent(MainActivity.this, AdminActivity.class));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startActivityByName(String className) {
        try {
            Class<?> activityClass = Class.forName(className);
            startActivity(new Intent(MainActivity.this, activityClass));
        } catch (ClassNotFoundException e) {
            String shortName = className.substring(className.lastIndexOf('.') + 1);
            Toast.makeText(this, shortName + " feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        try {
            sessionManager.logout();
            mAuth.signOut();
            try {
                Class<?> loginActivityClass = Class.forName("com.example.recipemasterpro.activities.LoginActivity");
                Intent intent = new Intent(MainActivity.this, loginActivityClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            } catch (ClassNotFoundException e) {
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
