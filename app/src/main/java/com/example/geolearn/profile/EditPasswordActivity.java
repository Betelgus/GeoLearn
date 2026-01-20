package com.example.geolearn.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.example.geolearn.auth.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditPasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPass, etNewPass, etConfirmPass;
    private MaterialButton btnUpdate;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_password);

        mAuth = FirebaseAuth.getInstance();

        // 1. Check for Guest Mode
        if (UserSession.isGuestMode(this)) {
            Toast.makeText(this, "Guest users cannot change passwords.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Initialize Views (ensure IDs match edit_password.xml)
        etCurrentPass = findViewById(R.id.etCurrentPassword);
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnUpdate = findViewById(R.id.btnUpdatePassword);

        // Optional: Back button support if it exists in your XML
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        btnUpdate.setOnClickListener(v -> handlePasswordChange());
    }

    private void handlePasswordChange() {
        String currentPass = etCurrentPass.getText() != null ? etCurrentPass.getText().toString().trim() : "";
        String newPass = etNewPass.getText() != null ? etNewPass.getText().toString().trim() : "";
        String confirmPass = etConfirmPass.getText() != null ? etConfirmPass.getText().toString().trim() : "";

        // 3. Validation
        if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            etConfirmPass.setError("Passwords do not match");
            return;
        }

        if (newPass.length() < 6) {
            etNewPass.setError("Password too short (min 6 characters)");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            btnUpdate.setEnabled(false);
            Toast.makeText(this, "Verifying...", Toast.LENGTH_SHORT).show();

            // 4. Re-authenticate user before changing password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // 5. Update Password
                    user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnUpdate.setEnabled(true);
                            Toast.makeText(this, "Error: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    btnUpdate.setEnabled(true);
                    etCurrentPass.setError("Incorrect current password");
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
