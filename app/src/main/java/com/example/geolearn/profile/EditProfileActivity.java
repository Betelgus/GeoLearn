package com.example.geolearn.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.example.geolearn.auth.UserSession;
import com.example.geolearn.database.AppDatabase;
import com.example.geolearn.database.entities.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private TextInputEditText etUsername, etEmail, etAge;
    private MaterialButton btnSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_layout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            // This closes EditProfileActivity and returns the user to SettingsActivity
            finish();
        });

        loadUserData();

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserData() {
        if (UserSession.isGuestMode(this)) {
            etUsername.setText("Guest User");
            etEmail.setText("guest@geolearn.com");
            etAge.setText("0");
            disableFields();
            return;
        }

        // 1. Try Intent Extras first (if passed from Profile/Settings)
        String currentName = getIntent().getStringExtra("current_username");
        String currentEmail = getIntent().getStringExtra("current_email");
        String currentAge = getIntent().getStringExtra("current_age");

        if (currentName != null && currentEmail != null) {
            etUsername.setText(currentName);
            etEmail.setText(currentEmail);
            etAge.setText(currentAge);
        } else {
            // 2. Fetch from Databases
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                
                // Check Local DB first (Room)
                new Thread(() -> {
                    User user = localDb.userDao().getUserById(uid);
                    if (user != null) {
                        runOnUiThread(() -> {
                            etUsername.setText(user.username);
                            etEmail.setText(user.email);
                            etAge.setText(String.valueOf(user.age));
                        });
                    } else {
                        // 3. Fallback to Firestore
                        fetchFromFirestore(uid);
                    }
                }).start();
            }
        }
    }

    private void fetchFromFirestore(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        Long age = documentSnapshot.getLong("age");

                        runOnUiThread(() -> {
                            if (name != null) etUsername.setText(name);
                            if (email != null) etEmail.setText(email);
                            if (age != null) etAge.setText(String.valueOf(age));
                        });

                        // Cache it locally
                        if (name != null && email != null && age != null) {
                            saveUserLocally(uid, name, email, age.intValue());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed", e));
    }

    private void disableFields() {
        etUsername.setEnabled(false);
        etEmail.setEnabled(false);
        etAge.setEnabled(false);
        btnSave.setEnabled(false);
        btnSave.setAlpha(0.5f);
    }

    private void saveProfileChanges() {
        String newName = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String newAgeStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";

        // Validation
        if (TextUtils.isEmpty(newName)) {
            etUsername.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEmail.setError("Valid email required");
            return;
        }
        if (TextUtils.isEmpty(newAgeStr)) {
            etAge.setError("Age required");
            return;
        }

        int newAge;
        try {
            newAge = Integer.parseInt(newAgeStr);
        } catch (NumberFormatException e) {
            etAge.setError("Invalid age");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        btnSave.setEnabled(false);
        String uid = currentUser.getUid();
        String oldEmail = currentUser.getEmail();

        // If email changed, we need to update Auth first
        if (oldEmail != null && !oldEmail.equalsIgnoreCase(newEmail)) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateFirestoreAndLocal(uid, newName, newEmail, newAge);
                        } else {
                            btnSave.setEnabled(true);
                            Log.e(TAG, "Email update failed", task.getException());
                            Toast.makeText(this, "Security: Please log out and back in to change email.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Just update info
            updateFirestoreAndLocal(uid, newName, oldEmail, newAge);
        }
    }

    private void updateFirestoreAndLocal(String uid, String name, String email, int age) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("age", age);
        updates.put("email", email);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    saveUserLocally(uid, name, email, age);
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify caller
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserLocally(String uid, String username, String email, int age) {
        new Thread(() -> {
            try {
                User updatedUser = new User(uid, username, email, age);
                localDb.userDao().insertUser(updatedUser);
            } catch (Exception e) {
                Log.e(TAG, "Room Update Error: " + e.getMessage());
            }
        }).start();
    }
}
