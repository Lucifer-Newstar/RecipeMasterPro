package com.example.recipemasterpro.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.activities.LoginActivity;
import com.example.recipemasterpro.activities.MainActivity;
import com.example.recipemasterpro.fragments.admin.AnalyticsFragment;
import com.example.recipemasterpro.fragments.admin.ReportsFragment;
import com.example.recipemasterpro.fragments.admin.UserManagementFragment;
import com.example.recipemasterpro.fragments.admin.ContentModerationFragment;
import com.example.recipemasterpro.fragments.admin.ReportedContentFragment;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SessionManager sessionManager;
    private TextView userNameText, userEmailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = SessionManager.getInstance(this);

        // Check if user is admin
        if (!Constants.ROLE_ADMIN.equals(sessionManager.getUserRole())) {
            // Not an admin, redirect to main
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupDrawer();
        loadUserInfo();

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, new AnalyticsFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_analytics);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Analytics");
            }
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        View headerView = navigationView.getHeaderView(0);
        userNameText = headerView.findViewById(R.id.userNameText);
        userEmailText = headerView.findViewById(R.id.userEmailText);
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadUserInfo() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name != null) userNameText.setText(name);
        if (email != null) userEmailText.setText(email);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String title = "";

        int id = item.getItemId();
        if (id == R.id.nav_analytics) {
            selectedFragment = new AnalyticsFragment();
            title = "Analytics";
        } else if (id == R.id.nav_users) {
            selectedFragment = new UserManagementFragment();
            title = "User Management";
        } else if (id == R.id.nav_recipes) {
            selectedFragment = ContentModerationFragment.newInstance("recipes");
            title = "Recipe Moderation";
        } else if (id == R.id.nav_reels) {
            selectedFragment = ContentModerationFragment.newInstance("reels");
            title = "Reel Moderation";
        } else if (id == R.id.nav_reports) {
            selectedFragment = new ReportedContentFragment();
            title = "Reported Content";
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, selectedFragment)
                    .commit();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        sessionManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
