package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView welcomeText;
    private Button actionButton1, actionButton2, actionButton3, logoutButton;
    private FirebaseAuth mAuth;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = SessionManager.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRoleBasedUI();
        setupClickListeners();
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        actionButton1 = findViewById(R.id.actionButton1);
        actionButton2 = findViewById(R.id.actionButton2);
        actionButton3 = findViewById(R.id.actionButton3);
        logoutButton = findViewById(R.id.logoutButton);
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
                    break;

                case Constants.ROLE_CHEF:
                    welcomeText.setText("Welcome Chef! 👨‍🍳");
                    actionButton1.setText("➕ Add New Recipe");
                    actionButton2.setText("📚 My Recipes");
                    actionButton3.setText("📈 Statistics");
                    break;

                case Constants.ROLE_USER:
                    welcomeText.setText("Welcome Food Lover! 🍳");
                    actionButton1.setText("🔍 Browse Recipes");
                    actionButton2.setText("❤️ My Favorites");
                    actionButton3.setText("🛒 Shopping List");
                    break;

                default:
                    welcomeText.setText("Welcome!");
            }
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
    }

    private void handleButton1Click() {
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: Add New Recipe
                Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: Browse Recipes
                Intent intent = new Intent(MainActivity.this, RecipeListActivity.class);
                startActivity(intent);
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: Manage Users
                Toast.makeText(this, "👥 Admin: User Management coming soon!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleButton2Click() {
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: My Recipes
                Intent intent = new Intent(MainActivity.this, MyRecipesActivity.class);
                startActivity(intent);
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: My Favorites
                Toast.makeText(this, "❤️ Favorites feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: Manage Recipes
                Toast.makeText(this, "📋 Admin: Recipe Management coming soon!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleButton3Click() {
        try {
            if (userRole.equals(Constants.ROLE_CHEF)) {
                // Chef: Recipe Statistics
                Toast.makeText(this, "📈 Statistics feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_USER)) {
                // User: Shopping List
                Toast.makeText(this, "🛒 Shopping List feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (userRole.equals(Constants.ROLE_ADMIN)) {
                // Admin: View Analytics
                Toast.makeText(this, "📊 Analytics feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        try {
            sessionManager.logout();
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}