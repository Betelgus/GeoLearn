package com.example.geolearn.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.home.MainMenuActivity;
import com.example.geolearn.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private CheckBox cbTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Initialize Views
        // Note: Ensure these IDs exist in your activity_register.xml
        etName = findViewById(R.id.etFullName); // You might need to add this ID to your XML
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // 2. Handle Sign Up Button
        btnSignUp.setOnClickListener(v -> {
            if (validateRegistration()) {
                // --- REGISTRATION LOGIC HERE ---
                // Save user data to database...

                completeRegistration();
            }
        });

        // 3. Handle "Already have an account?" Click
        tvLogin.setOnClickListener(v -> {
            // Just close this activity to go back to Login
            finish();
        });
    }

    private boolean validateRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void completeRegistration() {
        // 1. Save Session as "Registered User"
        UserSession.setGuestMode(this, false);

        // 2. Show Success
        Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

        // 3. Navigate to Main Menu
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}