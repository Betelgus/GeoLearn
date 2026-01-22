package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

// --- Imports for Auth & Database ---
import com.example.geolearn.auth.LoginActivity;
import com.example.geolearn.auth.UserSession;
import com.example.geolearn.feedback.FeedbackActivity;
import com.example.geolearn.feedback.FeedbackHistory;
import com.example.geolearn.game.FlashcardSelectionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
// --------------------------------

import com.example.geolearn.profile.BookmarksActivity;
import com.example.geolearn.game.GameAnalysisActivity;
import com.example.geolearn.game.GameCategoryActivity;
import com.example.geolearn.profile.ProgressDashboardActivity;
import com.example.geolearn.R;
import com.example.geolearn.profile.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    // --- NEW: Dashboard UI Elements ---
    private TextView tvMasteryPercent;
    private ProgressBar pbMastery;

    // --- NEW: Firebase Database ---
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 1. Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // 2. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 3. Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // 4. --- NEW: Bind Dashboard Views ---
        tvMasteryPercent = findViewById(R.id.tvMasteryPercent);
        pbMastery = findViewById(R.id.pbMastery);

        // 5. Setup Card Click Listeners
        setupClickListeners();
    }

    // --- NEW: Update Dashboard when returning to menu ---
    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardCard();
    }

    private void updateDashboardCard() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Reset if not logged in
            tvMasteryPercent.setText("0%");
            pbMastery.setProgress(0);
            return;
        }

        // Fetch all scores for this user to calculate "Overall Mastery"
        db.collection("Scores")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long totalEarned = 0;
                        long totalPossible = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long score = document.getLong("score");
                            Long totalQ = document.getLong("totalQuestions");

                            if (score != null && totalQ != null) {
                                totalEarned += score;
                                totalPossible += totalQ;
                            }
                        }

                        // Calculate Percentage
                        int percentage = 0;
                        if (totalPossible > 0) {
                            percentage = (int) ((totalEarned * 100) / totalPossible);
                        }

                        // Update the Card UI
                        tvMasteryPercent.setText(percentage + "%");
                        pbMastery.setProgress(percentage);
                    } else {
                        Log.e("MainMenu", "Error getting score documents: ", task.getException());
                    }
                });
    }

    private void setupClickListeners() {
        // Dashboard Card -> Goes to detailed progress page
        findViewById(R.id.cardDashboard).setOnClickListener(v ->
                startActivity(new Intent(this, ProgressDashboardActivity.class)));

        findViewById(R.id.cardQuiz).setOnClickListener(v -> showDifficultyBottomSheet());

        findViewById(R.id.cardFlashcard).setOnClickListener(v ->
                startActivity(new Intent(this, FlashcardSelectionActivity.class)));

        findViewById(R.id.cardGameAnalysis).setOnClickListener(v ->
                startActivity(new Intent(this, GameAnalysisActivity.class)));

        findViewById(R.id.cardBookmarks).setOnClickListener(v ->
                startActivity(new Intent(this, BookmarksActivity.class)));

        findViewById(R.id.cardFeedback).setOnClickListener(v -> {
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
            startQuiz("Beginner");
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.cardIntermediate).setOnClickListener(v -> {
            startQuiz("Intermediate");
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.cardAdvanced).setOnClickListener(v -> {
            startQuiz("Advanced");
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void startQuiz(String difficulty) {
        Intent intent = new Intent(MainMenuActivity.this, GameCategoryActivity.class);
        intent.putExtra("DIFFICULTY", difficulty);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already here
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_feedback) {
            startActivity(new Intent(this, FeedbackHistory.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            performLogout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        UserSession.clear(this);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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