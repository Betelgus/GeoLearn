package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

// --- Imports for Logout Logic ---
import com.example.geolearn.auth.LoginActivity;
import com.example.geolearn.auth.UserSession;
import com.google.firebase.auth.FirebaseAuth;
// --------------------------------

import com.example.geolearn.profile.BookmarksActivity;
import com.example.geolearn.game.FlashcardActivity;
import com.example.geolearn.game.GameAnalysisActivity;
import com.example.geolearn.game.GameCategoryActivity;
import com.example.geolearn.profile.ProgressDashboardActivity;
import com.example.geolearn.R;
import com.example.geolearn.profile.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Setup Sidebar (Drawer)
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // 3. --- EXISTING CARD LISTENERS ---
        findViewById(R.id.cardDashboard).setOnClickListener(v ->
                startActivity(new Intent(this, ProgressDashboardActivity.class)));

        findViewById(R.id.cardQuiz).setOnClickListener(v -> showDifficultyBottomSheet());

        findViewById(R.id.cardFlashcard).setOnClickListener(v ->
                startActivity(new Intent(this, FlashcardActivity.class)));

        findViewById(R.id.cardGameAnalysis).setOnClickListener(v ->
                startActivity(new Intent(this, GameAnalysisActivity.class)));

        findViewById(R.id.cardBookmarks).setOnClickListener(v ->
                startActivity(new Intent(this, BookmarksActivity.class)));

        // 4. --- FEEDBACK LISTENER ---
        findViewById(R.id.cardFeedback).setOnClickListener(v -> {
            // Optional: Check if guest before allowing feedback
            Intent intent = new Intent(MainMenuActivity.this, FeedbackActivity.class);
            intent.putExtra("IS_GUEST", UserSession.isGuestMode(this));
            startActivity(intent);
        });
    }

    private void showDifficultyBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_difficulty_sheet, null);
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
        Intent intent = new Intent(MainMenuActivity.this, GameCategoryActivity.class);
        intent.putExtra("DIFFICULTY_LEVEL", level);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            performLogout(); // <--- Call the logout method
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // --- LOGOUT LOGIC ---
    private void performLogout() {
        // 1. Clear Local Session (SharedPreferences)
        UserSession.clear(this);

        // 2. Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // 3. Navigate back to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        // FLAG_ACTIVITY_CLEAR_TASK clears the existing activity stack so the user cannot press "Back" to return here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 4. Close this activity
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}