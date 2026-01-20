package com.example.geolearn.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.home.MainMenuActivity;
import com.example.geolearn.R;
import com.example.geolearn.database.AppDatabase;
import com.example.geolearn.database.entities.User;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;

    // 1. Declare Firebase & Database instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Initialize Firebase & Local DB
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getInstance(this);

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvGuest = findViewById(R.id.tvGuest);

        // Handle Login Button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(email, password)) {
                performLogin(email, password);
            }
        });

        // Handle Register Link
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle Guest Mode
        tvGuest.setOnClickListener(v -> {
            UserSession.setGuestMode(this, true); //
            Toast.makeText(this, "Welcome, Guest!", Toast.LENGTH_SHORT).show();
            navigateToHome();
        });
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false); // Disable button to prevent multiple clicks
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        // 3. Authenticate with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // 4. Auth successful, now fetch extra data (Name) from Firestore
                            fetchUserFromFirestore(user.getUid());
                        }
                    } else {
                        // Login failed
                        btnLogin.setEnabled(true);
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication Failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserFromFirestore(String uid) {
        // Access the "users" collection defined in your RegisterActivity
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            // 5. Retrieve fields stored during registration
                            String name = document.getString("name"); //
                            String email = document.getString("email");

                            // 6. Save to Local SQLite (Room) so app works offline
                            saveUserLocally(uid, name, email);

                            // 7. Complete Login
                            UserSession.setGuestMode(LoginActivity.this, false);
                            Toast.makeText(LoginActivity.this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                            navigateToHome();

                        } else {
                            Log.e(TAG, "No user document found in Firestore");
                            // Allow login even if Firestore data is missing, but warn
                            UserSession.setGuestMode(LoginActivity.this, false);
                            navigateToHome();
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        Log.e(TAG, "Firestore get failed: ", task.getException());
                        Toast.makeText(LoginActivity.this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserLocally(String uid, String name, String email) {
        // Run database operations in a background thread to avoid blocking UI
        new Thread(() -> {
            try {
                User localUser = new User(uid, name, email);
                localDb.userDao().insertUser(localUser); //
                Log.d(TAG, "User synced to local SQLite DB");
            } catch (Exception e) {
                Log.e(TAG, "Error saving user locally: " + e.getMessage());
            }
        }).start();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}