package com.example.geolearn.profile;

import android.content.Intent;
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

        // 2. Account Actions
        // Edit Profile Listener
        findViewById(R.id.rowEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Change Password Listener (Moved outside to fix nesting and variable conflict)
        findViewById(R.id.rowChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditPasswordActivity.class);
            startActivity(intent);
        });


    }
}