package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geolearn.R;
import com.example.geolearn.auth.LoginActivity;
import com.example.geolearn.auth.UserSession;
import com.example.geolearn.feedback.FeedbackActivity;
import com.example.geolearn.feedback.FeedbackHistory;
import com.example.geolearn.feedback.feedback;
import com.example.geolearn.game.FlashcardSelectionActivity;
import com.example.geolearn.game.GameAnalysisActivity;
import com.example.geolearn.game.GameCategoryActivity;
import com.example.geolearn.profile.BookmarksActivity;
import com.example.geolearn.profile.ProgressDashboardActivity;
import com.example.geolearn.profile.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView tvMasteryPercent;
    private ProgressBar pbMastery;

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

        // --- NEW: Update Header ---
        updateNavHeader();

        // 4. Bind Dashboard Views
        tvMasteryPercent = findViewById(R.id.tvMasteryPercent);
        pbMastery = findViewById(R.id.pbMastery);

        // 5. Setup Card Click Listeners
        setupClickListeners();

        // Update Dashboard Data
        updateDashboardCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardCard();
        // Also refresh header in case user changed profile details
        updateNavHeader();
        // Reload feedback in case user added one recently
        loadCommunityFeedback();
    }

    /**
     * Updates the Navigation Header with Username and Email from Firestore.
     */
    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView navName = headerView.findViewById(R.id.nav_header_name);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);
        ImageView navImage = headerView.findViewById(R.id.nav_header_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isAnonymous()) {
                navName.setText("Guest");
                navEmail.setText("Sign up to save progress");
                navImage.setImageResource(R.mipmap.ic_launcher_round);
            } else {
                String userId = user.getUid();
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Fetch 'username' field
                                String username = documentSnapshot.getString("username");
                                String email = documentSnapshot.getString("email");

                                if (username != null && !username.isEmpty()) {
                                    navName.setText(username);
                                } else {
                                    navName.setText("GeoStudent");
                                }

                                if (email != null && !email.isEmpty()) {
                                    navEmail.setText(email);
                                } else {
                                    navEmail.setText(user.getEmail());
                                }

                                // Set the User Icon
                                navImage.setImageResource(android.R.drawable.ic_menu_myplaces);
                            }
                        })
                        .addOnFailureListener(e -> Log.e("MainMenu", "Error loading user header", e));
            }
        }
    }

    // --- YOUR ORIGINAL DASHBOARD CODE (UNCHANGED) ---
    private void updateDashboardCard() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (tvMasteryPercent != null) tvMasteryPercent.setText("0%");
            if (pbMastery != null) pbMastery.setProgress(0);
            return;
        }

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

                        int percentage = 0;
                        if (totalPossible > 0) {
                            percentage = (int) ((totalEarned * 100) / totalPossible);
                        }

                        if (tvMasteryPercent != null) tvMasteryPercent.setText(percentage + "%");
                        if (pbMastery != null) pbMastery.setProgress(percentage);
                    } else {
                        Log.e("MainMenu", "Error getting score documents: ", task.getException());
                    }
                });
    }

    private void setupClickListeners() {
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
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                // Navigate to the FeedbackActivity which uses activity_feedback.xml
                Intent intent = new Intent(MainMenuActivity.this, FeedbackActivity.class);
                startActivity(intent);
            });
        }
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
        intent.putExtra("DIFFICULTY_LEVEL", difficulty);
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
