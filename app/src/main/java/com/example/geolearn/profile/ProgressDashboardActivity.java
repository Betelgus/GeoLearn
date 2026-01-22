package com.example.geolearn.profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgressDashboardActivity extends AppCompatActivity {

    // UI Elements
    private TextView tvStreakTitle, tvStreakSubtitle;
    private TextView tvBeginnerPercent, tvIntermediatePercent, tvAdvancedPercent;
    private ProgressBar pbBeginner, pbIntermediate, pbAdvanced;

    // Firebase
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_dashboard);

        // 1. Initialize Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
        } else {
            Toast.makeText(this, "Please login to view progress", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Bind Views
        initViews();

        // 3. Fetch Data from Firestore
        fetchProgressData();
    }
        private void initViews() {
            // --- UPDATED BACK BUTTON LOGIC ---
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            // This handles the click on the arrow icon we added in XML
            toolbar.setNavigationOnClickListener(v -> finish());
            // ---------------------------------

            tvStreakTitle = findViewById(R.id.tvStreakTitle);
            tvStreakSubtitle = findViewById(R.id.tvStreakSubtitle);

            tvBeginnerPercent = findViewById(R.id.tvBeginnerPercent);
            pbBeginner = findViewById(R.id.pbBeginner);

            tvIntermediatePercent = findViewById(R.id.tvIntermediatePercent);
            pbIntermediate = findViewById(R.id.pbIntermediate);

            tvAdvancedPercent = findViewById(R.id.tvAdvancedPercent);
            pbAdvanced = findViewById(R.id.pbAdvanced);
        }


    private void fetchProgressData() {
        // Query the "Scores" collection for the current user
        db.collection("Scores")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        processGameData(task.getResult());
                    } else {
                        Log.e("ProgressDashboard", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to load progress.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processGameData(Iterable<QueryDocumentSnapshot> documents) {
        // Variables to calculate mastery
        float beginnerScore = 0, beginnerTotal = 0;
        float intermediateScore = 0, intermediateTotal = 0;
        float advancedScore = 0, advancedTotal = 0;

        // Set to store unique dates played for streak calculation
        Set<String> uniqueDatesPlayed = new HashSet<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        for (QueryDocumentSnapshot document : documents) {
            String difficulty = document.getString("difficulty");
            Long score = document.getLong("score");
            Long totalQ = document.getLong("totalQuestions");

            // --- SAFETY FIX FOR TIMESTAMP CRASH ---
            Date timestamp = null;
            Object ts = document.get("timestamp");

            if (ts instanceof com.google.firebase.Timestamp) {
                // New correct format
                timestamp = ((com.google.firebase.Timestamp) ts).toDate();
            } else if (ts instanceof Long) {
                // Old number format (prevents crash on old data)
                timestamp = new Date((Long) ts);
            }
            // --------------------------------------

            // 1. Aggregate Scores
            if (difficulty != null && score != null && totalQ != null) {
                switch (difficulty) {
                    case "Beginner":
                        beginnerScore += score;
                        beginnerTotal += totalQ;
                        break;
                    case "Intermediate":
                        intermediateScore += score;
                        intermediateTotal += totalQ;
                        break;
                    case "Advanced":
                        advancedScore += score;
                        advancedTotal += totalQ;
                        break;
                }
            }

            // 2. Collect Date for Streak
            if (timestamp != null) {
                uniqueDatesPlayed.add(sdf.format(timestamp));
            }
        }

        // 3. Update UI
        updateMasteryUI(beginnerScore, beginnerTotal, tvBeginnerPercent, pbBeginner);
        updateMasteryUI(intermediateScore, intermediateTotal, tvIntermediatePercent, pbIntermediate);
        updateMasteryUI(advancedScore, advancedTotal, tvAdvancedPercent, pbAdvanced);

        calculateAndSetStreak(uniqueDatesPlayed);
    }

    private void updateMasteryUI(float earned, float total, TextView tvPercent, ProgressBar pb) {
        int percentage = 0;
        if (total > 0) {
            percentage = (int) ((earned / total) * 100);
        }

        // Update View
        tvPercent.setText(percentage + "%");
        pb.setProgress(percentage);
    }

    private void calculateAndSetStreak(Set<String> uniqueDatesPlayed) {
        // Simple streak logic: check consecutive days backwards from today
        List<String> sortedDates = new ArrayList<>(uniqueDatesPlayed);
        Collections.sort(sortedDates, Collections.reverseOrder()); // Newest first

        int streak = 0;
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        // Check if played today
        String todayStr = sdf.format(calendar.getTime());

        // If user hasn't played today, we check if they played yesterday to maintain streak
        if (!uniqueDatesPlayed.contains(todayStr)) {
            // Move calendar back 1 day to check yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Loop backwards to count streak
        while (true) {
            String checkDate = sdf.format(calendar.getTime());
            if (uniqueDatesPlayed.contains(checkDate)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1); // Go to previous day
            } else {
                break; // Streak broken
            }
        }

        tvStreakTitle.setText(streak + " Day Streak!");
        if (streak > 0) {
            tvStreakSubtitle.setText("Great job! Keep the momentum going.");
        } else {
            tvStreakSubtitle.setText("Play a game today to start a streak!");
        }
    }
}