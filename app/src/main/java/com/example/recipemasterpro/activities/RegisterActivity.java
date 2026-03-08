package com.example.recipemasterpro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private RadioGroup roleRadioGroup;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        int selectedId = roleRadioGroup.getCheckedRadioButtonId();
        String role;
        if (selectedId == R.id.chefRadioButton) {
            role = Constants.ROLE_CHEF;
        } else {
            role = Constants.ROLE_USER;
        }

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create user with ALL fields
                            User user = new User(
                                    firebaseUser.getUid(),
                                    name,
                                    email,
                                    role
                            );
                            
                            // Set default values for all fields
                            user.setRecipeCount(0);
                            user.setReelCount(0);
                            user.setFollowersCount(0);
                            user.setFollowingCount(0);
                            user.setTotalLikes(0);
                            user.setCreatedAt(System.currentTimeMillis());
                            user.setLastLogin(System.currentTimeMillis());

                            db.collection(Constants.USERS_COLLECTION)
                                    .document(firebaseUser.getUid())
                                    .set(user)
                                    .addOnCompleteListener(task1 -> {
                                        progressBar.setVisibility(View.GONE);
                                        registerButton.setEnabled(true);

                                        if (task1.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Failed to save user data", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
