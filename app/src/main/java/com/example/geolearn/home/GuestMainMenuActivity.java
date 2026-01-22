package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.geolearn.feedback.FeedbackActivity;
import com.example.geolearn.game.FlashcardSelectionActivity; // Make sure this matches your package
import com.example.geolearn.game.GameCategoryActivity;
import com.example.geolearn.R;
import com.example.geolearn.auth.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

public class GuestMainMenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_guest);

        // Setup Toolbar & Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Guest Navigation logic
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(this, AboutActivity.class));
            } else if (id == R.id.nav_exit) {
                finishAffinity();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // --- GUEST CARD LISTENERS ---

        // Locked features prompt login
        findViewById(R.id.btnCreateAcc).setOnClickListener(v -> redirectToLogin("Login to see dashboard"));
        findViewById(R.id.cardLockedAnalysis).setOnClickListener(v -> redirectToLogin("Login to see analysis"));
        findViewById(R.id.cardLockedBookmarks).setOnClickListener(v -> redirectToLogin("Login to use bookmarks"));

        // Open features
        findViewById(R.id.cardQuiz).setOnClickListener(v -> showDifficultyBottomSheet());

        // --- FIXED: Point to Selection Screen ---
        findViewById(R.id.cardFlashcard).setOnClickListener(v -> {
            // 1. Go to SELECTION ACTIVITY, not the game directly
            Intent intent = new Intent(GuestMainMenuActivity.this, FlashcardSelectionActivity.class);
            // 2. Pass the FORCE_GUEST flag so the next screen knows to hide bookmarks later
            intent.putExtra("FORCE_GUEST", true);
            startActivity(intent);
        });

        // Feedback Listener
        findViewById(R.id.cardFeedback).setOnClickListener(v -> {
            Intent intent = new Intent(GuestMainMenuActivity.this, FeedbackActivity.class);
            intent.putExtra("IS_GUEST", true);
            startActivity(intent);
        });
    }

    private void redirectToLogin(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showDifficultyBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_difficulty_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.cardBeginner).setOnClickListener(v -> {
            startQuizWithDifficulty("Beginner");
            bottomSheetDialog.dismiss();
        });
        sheetView.findViewById(R.id.cardIntermediate).setOnClickListener(v -> {
            startQuizWithDifficulty("Intermediate");
            bottomSheetDialog.dismiss();
        });
        sheetView.findViewById(R.id.cardAdvanced).setOnClickListener(v -> {
            startQuizWithDifficulty("Advanced");
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void startQuizWithDifficulty(String level) {
        Intent intent = new Intent(this, GameCategoryActivity.class);
        intent.putExtra("DIFFICULTY_LEVEL", level);
        intent.putExtra("IS_GUEST_MODE", true);
        startActivity(intent);
    }
}