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

import com.example.geolearn.home.GuestMainMenuActivity;
import com.example.geolearn.home.MainMenuActivity;
import com.example.geolearn.R;
import com.example.geolearn.database.AppDatabase;
import com.example.geolearn.database.entities.User;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword; // Changed from etEmail
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
        // IMPORTANT: Ensure your XML has an EditText with id 'etUsername' (or change this line to match your XML)
        etUsername = findViewById(R.id.etUsername); // Keeping ID as etEmail to prevent crash if XML isn't updated, but logically it's username
        etUsername.setHint("Username"); // Set hint programmatically just in case

        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvGuest = findViewById(R.id.tvGuest);

        // Handle Login Button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(username, password)) {
                loginWithUsername(username, password);
            }
        });

        // Handle Register Link
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle Guest Mode
        tvGuest.setOnClickListener(v -> {
            // 1. Set session to GUEST mode
            UserSession.setGuestMode(this, true);

            Toast.makeText(this, "Welcome, Guest!", Toast.LENGTH_SHORT).show();

            // 2. CHANGE: Point to the new Guest Activity
            Intent intent = new Intent(this, GuestMainMenuActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    // 3. New Logic: Lookup Email via Username -> Then Login
    private void loginWithUsername(String username, String password) {
        btnLogin.setEnabled(false);
        Toast.makeText(this, "Verifying username...", Toast.LENGTH_SHORT).show();

        // Query Firestore for user with matching 'username' field
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            // User found
                            DocumentSnapshot userDoc = documents.getDocuments().get(0);
                            String email = userDoc.getString("email");
                            String uid = userDoc.getId();

                            // Get other fields now to save later
                            Long ageLong = userDoc.getLong("age");
                            int age = (ageLong != null) ? ageLong.intValue() : 0;

                            if (email != null) {
                                // Proceed to actual Authentication
                                performAuth(email, password, uid, username, age);
                            } else {
                                handleError("Email not linked to this username.");
                            }
                        } else {
                            handleError("Username not found.");
                        }
                    } else {
                        handleError("Connection failed: " + task.getException().getMessage());
                    }
                });
    }

    private void performAuth(String email, String password, String uid, String username, int age) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        // 4. Save to Local SQLite (Room)
                        saveUserLocally(uid, username, email, age);

                        // 5. Complete Login
                        UserSession.setGuestMode(LoginActivity.this, false);
                        Toast.makeText(LoginActivity.this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        handleError("Incorrect Password.");
                    }
                });
    }

    private void handleError(String message) {
        btnLogin.setEnabled(true);
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveUserLocally(String uid, String username, String email, int age) {
        new Thread(() -> {
            try {
                User localUser = new User(uid, username, email, age);
                localDb.userDao().insertUser(localUser);
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