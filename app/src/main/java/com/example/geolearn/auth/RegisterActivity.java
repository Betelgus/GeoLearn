package com.example.geolearn.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText etName, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    // Change from DatabaseReference to FirebaseFirestore
    private FirebaseFirestore db;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        // Initialize FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Initialize Local SQLite Database (Room)
        localDb = AppDatabase.getInstance(this);

        // 1. Initialize Views
        etName = findViewById(R.id.etFullName);
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
        tvLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean validateRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
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
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnSignUp.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth: User created successfully");
                        String userId = mAuth.getCurrentUser().getUid();

                        // 1. Save to Firestore (Remote)
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);

                        // Use db.collection().document().set() for Firestore
                        db.collection("users").document(userId).set(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Log.d(TAG, "Firestore: User data saved");
                                        // 2. Save to SQLite (Local)
                                        saveUserLocally(userId, name, email);
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

    private void saveUserLocally(String uid, String name, String email) {
        try {
            User localUser = new User(uid, name, email);
            localDb.userDao().insertUser(localUser);
            Log.d(TAG, "SQLite: User saved locally");
        } catch (Exception e) {
            Log.e(TAG, "SQLite Error: " + e.getMessage());
        }
    }

    private void completeRegistration() {
        UserSession.setGuestMode(this, false);
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
