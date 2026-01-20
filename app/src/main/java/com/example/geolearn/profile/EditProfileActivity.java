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

        loadUserData();

        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserData() {
        if (UserSession.isGuestMode(this)) {
            etUsername.setHint("Guest User");
            etEmail.setHint("guest@geolearn.com");
            etAge.setHint("0");
            disableFields();
            return;
        }

        // Try Intent first, then Local DB
        String currentName = getIntent().getStringExtra("current_username");
        String currentEmail = getIntent().getStringExtra("current_email");
        String currentAge = getIntent().getStringExtra("current_age");

        if (currentName != null) {
            etUsername.setHint(currentName);
            etEmail.setHint(currentEmail);
            etAge.setHint(currentAge);
            
            // Disable email field as discussed
            etEmail.setEnabled(false);
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                new Thread(() -> {
                    User user = localDb.userDao().getUserById(currentUser.getUid());
                    if (user != null) {
                        runOnUiThread(() -> {
                            etUsername.setHint(user.username);
                            etEmail.setHint(user.email);
                            etAge.setHint(String.valueOf(user.age));
                            
                            // Disable email field
                            etEmail.setEnabled(false);
                            etEmail.setText(user.email); // Keep text in email so they know what it is
                        });
                    }
                }).start();
            }
        }
    }

    private void disableFields() {
        etUsername.setEnabled(false);
        etEmail.setEnabled(false);
        etAge.setEnabled(false);
        btnSave.setEnabled(false);
    }

    private void saveProfileChanges() {
        String newName = etUsername.getText() != null && !etUsername.getText().toString().isEmpty() 
                ? etUsername.getText().toString().trim() 
                : (etUsername.getHint() != null ? etUsername.getHint().toString() : "");
                
        String newAgeStr = etAge.getText() != null && !etAge.getText().toString().isEmpty() 
                ? etAge.getText().toString().trim() 
                : (etAge.getHint() != null ? etAge.getHint().toString() : "");

        if (TextUtils.isEmpty(newName)) {
            etUsername.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(newAgeStr)) {
            etAge.setError("Age is required");
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
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();

        // Update Firestore and Local DB (Skipping Auth Email update as agreed)
        updateFirestoreAndLocal(uid, newName, email, newAge);
    }

    private void updateFirestoreAndLocal(String uid, String name, String email, int age) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("age", age);
        // We keep email in Firestore updated just in case, though it shouldn't change here
        updates.put("email", email);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    saveUserLocally(uid, name, email, age);
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserLocally(String uid, String username, String email, int age) {
        new Thread(() -> {
            try {
                User updatedUser = new User(uid, username, email, age);
                localDb.userDao().insertUser(updatedUser);
            } catch (Exception e) {
                Log.e(TAG, "Local DB Error: " + e.getMessage());
            }
        }).start();
    }
}
