package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// --- Imports for Logout Logic ---
import com.example.geolearn.auth.LoginActivity;
import com.example.geolearn.auth.UserSession;
import com.example.geolearn.feedback.feedback; // Ensure correct import
import com.example.geolearn.feedback.FeedbackActivity;
import com.example.geolearn.feedback.FeedbackHistory;
import com.example.geolearn.game.FlashcardSelectionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
// --------------------------------

import com.example.geolearn.profile.BookmarksActivity;
import com.example.geolearn.game.GameAnalysisActivity;
import com.example.geolearn.game.GameCategoryActivity;
import com.example.geolearn.profile.ProgressDashboardActivity;
import com.example.geolearn.R;
import com.example.geolearn.profile.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    // --- Feedback Variables ---
    private RecyclerView rvHomeFeedback;
    private HomeFeedbackAdapter homeAdapter;
    private List<feedback> homeFeedbackList;
    private FirebaseFirestore db;
    // --------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

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
                startActivity(new Intent(this, FlashcardSelectionActivity.class)));

        findViewById(R.id.cardGameAnalysis).setOnClickListener(v ->
                startActivity(new Intent(this, GameAnalysisActivity.class)));

        findViewById(R.id.cardBookmarks).setOnClickListener(v ->
                startActivity(new Intent(this, BookmarksActivity.class)));

        // 4. --- FEEDBACK SECTION SETUP ---

        // A. Setup RecyclerView
        rvHomeFeedback = findViewById(R.id.rvHomeFeedback);
        rvHomeFeedback.setLayoutManager(new LinearLayoutManager(this));
        homeFeedbackList = new ArrayList<>();
        homeAdapter = new HomeFeedbackAdapter(homeFeedbackList);
        rvHomeFeedback.setAdapter(homeAdapter);

        // B. Load Data from Firestore
        loadCommunityFeedback();

        // C. "Add Feedback" (Card Click)
        findViewById(R.id.cardFeedback).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, FeedbackActivity.class);
            intent.putExtra("IS_GUEST", UserSession.isGuestMode(this));
            startActivity(intent);
        });

        // D. "Tap to view all..." (Text Click)
        TextView tvViewAll = findViewById(R.id.tvTapToViewAll);
        tvViewAll.setOnClickListener(v -> {
            // Navigate to the FeedbackActivity which uses activity_feedback.xml
            Intent intent = new Intent(MainMenuActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }

    private void loadCommunityFeedback() {
        // 1. Fetch the 3 most recent feedbacks from Firestore
        db.collection("feedback")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // CLEAR ONLY WHEN DATA ARRIVES: This prevents the list from growing
                    // if onResume() and onCreate() both trigger requests.
                    homeFeedbackList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        homeAdapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        feedback f = doc.toObject(feedback.class);
                        if (f != null) {
                            fetchUsernameAndAddToList(f);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainMenu", "Error loading feedback", e));
    }

    private void fetchUsernameAndAddToList(feedback f) {
        db.collection("users").document(f.userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        f.username = userDoc.getString("username");
                    } else {
                        f.username = "Anonymous";
                    }

                    // Check again to ensure we don't exceed 3 locally
                    // (Optional safety check for async calls)
                    if (homeFeedbackList.size() < 3) {
                        homeFeedbackList.add(f);

                        // Sort locally so the newest still stays at the top
                        homeFeedbackList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                        homeAdapter.notifyDataSetChanged();
                    }
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

    // --- LOGOUT LOGIC ---
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload feedback in case user added one recently
        loadCommunityFeedback();
    }
}