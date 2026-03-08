package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.ErrorHandler;
import com.example.recipemasterpro.utils.FCMTokenManager;
import com.example.recipemasterpro.utils.NetworkUtils;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            checkUserStatusAndLogin(firebaseUser);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        ErrorHandler.handleFirestoreError(this, task.getException());
                    }
                });
    }

    private void checkUserStatusAndLogin(FirebaseUser firebaseUser) {
        db.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.isSuspended()) {
                            long currentTime = System.currentTimeMillis();
                            if (user.getSuspendedUntil() > currentTime) {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                                mAuth.signOut();
                                
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                                String dateStr = sdf.format(new java.util.Date(user.getSuspendedUntil()));
                                
                                String message = "Your account is suspended until " + dateStr + 
                                               ".\nReason: " + user.getSuspensionReason();
                                
                                new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("Account Suspended")
                                        .setMessage(message)
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            } else {
                                // Suspension expired
                                db.collection(Constants.USERS_COLLECTION)
                                        .document(firebaseUser.getUid())
                                        .update("isSuspended", false);
                            }
                        }
                        
                        // Proceed with login
                        db.collection(Constants.USERS_COLLECTION)
                                .document(firebaseUser.getUid())
                                .update("lastLogin", System.currentTimeMillis());
                        loadUserDataAndFinish(firebaseUser.getUid());
                        
                    } else {
                        // User doesn't exist in Firestore, create them
                        createUserFromAuth(firebaseUser);
                    }
                })
                .addOnFailureListener(e -> {
                    ErrorHandler.handleFirestoreError(this, e);
                    // Fallback attempt to load data
                    loadUserDataAndFinish(firebaseUser.getUid());
                });
    }

    private void createUserFromAuth(FirebaseUser firebaseUser) {
        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                firebaseUser.getEmail(),
                Constants.ROLE_USER // Default role
        );
        
        user.setCreatedAt(System.currentTimeMillis());
        user.setLastLogin(System.currentTimeMillis());
        user.setSuspended(false);
        
        db.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    loadUserDataAndFinish(firebaseUser.getUid());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    ErrorHandler.handleFirestoreError(this, e);
                });
    }

    private void loadUserDataAndFinish(String uid) {
        db.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        sessionManager.saveUserSession(
                                user.getUserId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRole()
                        );

                        // Initialize FCM token after successful login
                        FCMTokenManager tokenManager = new FCMTokenManager(LoginActivity.this);
                        tokenManager.initializeToken();

                        Toast.makeText(LoginActivity.this,
                                "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    ErrorHandler.handleFirestoreError(this, e);
                });
    }
}
