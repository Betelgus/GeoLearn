package com.example.geolearn.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GeoLearnPrefs";
    private static final String KEY_QUIZ_TIMER = "quiz_timer_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. Account Actions (Dummy)
        findViewById(R.id.rowEditProfile).setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowChangePassword).setOnClickListener(v ->
                Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show());

        // -------------------------------------------------------------
        // 3. PREFERENCES
        // -------------------------------------------------------------
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Notifications & Sound (Dummy Logic)
        SwitchMaterial switchNotifs = findViewById(R.id.switchNotifications);
        SwitchMaterial switchSound = findViewById(R.id.switchSound);

        switchNotifs.setOnCheckedChangeListener((v, isChecked) -> {
            // Save notification preference logic
        });

        // --- NEW: Quiz Timer Logic ---
        SwitchMaterial switchTimer = findViewById(R.id.switchTimer);

        // Set initial state
        boolean isTimerEnabled = prefs.getBoolean(KEY_QUIZ_TIMER, true);
        switchTimer.setChecked(isTimerEnabled);

        // Save on change
        switchTimer.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean(KEY_QUIZ_TIMER, isChecked).apply();
            String status = isChecked ? "Enabled" : "Disabled";
            Toast.makeText(this, "Quiz Timer " + status, Toast.LENGTH_SHORT).show();
        });

        // -------------------------------------------------------------
        // 4. DATA MANAGEMENT (NEW)
        // -------------------------------------------------------------

        // Clear Cache
        findViewById(R.id.rowClearCache).setOnClickListener(v -> {
            try {
                // Clear app cache directory
                getCacheDir().delete();
                Toast.makeText(this, "Cache Cleared", Toast.LENGTH_SHORT).show();

                // Reset cache text size to 0
                TextView tvSize = findViewById(R.id.tvCacheSize);
                tvSize.setText("0 MB");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Clear Local Data
        findViewById(R.id.rowClearData).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data?")
                    .setMessage("This will reset your settings, bookmarks, and quiz history. This cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        // Clear Settings Prefs
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();

                        // Clear Bookmarks (if using 'GeoLearnBookmarks' from previous steps)
                        getSharedPreferences("GeoLearnBookmarks", MODE_PRIVATE).edit().clear().apply();

                        Toast.makeText(this, "All Local Data Reset", Toast.LENGTH_SHORT).show();

                        // Reset UI State
                        switchTimer.setChecked(true); // Default
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // 5. Logout
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}