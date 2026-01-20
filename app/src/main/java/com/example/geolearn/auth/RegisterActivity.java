package com.example.geolearn.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.home.MainMenuActivity;
import com.example.geolearn.R;
import com.example.geolearn.database.AppDatabase;
import com.example.geolearn.database.entities.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText etUsername, etAge, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Local SQLite Database (Room)
        localDb = AppDatabase.getInstance(this);

        // 1. Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // 2. Handle Sign Up Button
        btnSignUp.setOnClickListener(v -> {
            Log.d(TAG, "Sign Up button clicked");
            if (validateRegistration()) {
                registerUser();
            }
        });

        // 3. Handle "Already have an account?" Click
        tvLogin.setOnClickListener(v -> finish());
    }

    private boolean validateRegistration() {
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String ageStr = Objects.requireNonNull(etAge.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser() {
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String ageStr = Objects.requireNonNull(etAge.getText()).toString().trim();
        int age = Integer.parseInt(ageStr);
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        btnSignUp.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        Log.d(TAG, "FirebaseAuth: User created successfully");
                        String userId = mAuth.getCurrentUser().getUid();

                        // 1. Save to Firestore (Remote)
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("age", age);
                        userMap.put("email", email);

                        db.collection("users").document(userId).set(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Log.d(TAG, "Firestore: User data saved");
                                        // 2. Save to SQLite (Local)
                                        saveUserLocally(userId, username, email, age);
                                        completeRegistration();
                                    } else {
                                        btnSignUp.setEnabled(true);
                                        String error = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown error";
                                        Log.e(TAG, "Firestore Error: " + error);
                                        Toast.makeText(RegisterActivity.this, "Database error: " + error, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        btnSignUp.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "FirebaseAuth Error: " + error);

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this, "Email already registered.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserLocally(String uid, String username, String email, int age) {
        try {
            User localUser = new User(uid, username, email, age);
            localDb.userDao().insertUser(localUser);
            Log.d(TAG, "SQLite: User saved locally");
        } catch (Exception e) {
            Log.e(TAG, "SQLite Error: " + e.getMessage());
        }
    }

    private void completeRegistration() {
        UserSession.setGuestMode(this, false);
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
