package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.example.geolearn.home.MainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FlagResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_result);

        // 1. Initialize Views
        TextView tvScoreFraction = findViewById(R.id.tvScoreFraction);
        TextView tvResultSubtitle = findViewById(R.id.tvResultSubtitle);
        TextView tvCorrect = findViewById(R.id.tvCorrect);
        TextView tvIncorrect = findViewById(R.id.tvIncorrect);
        TextView tvTime = findViewById(R.id.tvTime);
        ProgressBar progressScore = findViewById(R.id.progressScore);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // 2. Get Data from Intent
        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL_QUESTIONS", 10);
        String timeTaken = getIntent().getStringExtra("TIME_TAKEN");

        String difficulty = getIntent().getStringExtra("DIFFICULTY");
        if (difficulty == null) difficulty = "Beginner";

        if (timeTaken == null) timeTaken = "--:--";

        // 3. Set UI Logic
        if (score >= 0 && score <= 2) {
            tvResultSubtitle.setText("Learning starts with trying. You’re on the right path!");
        } else if (score >= 3 && score <= 5) {
            tvResultSubtitle.setText("Well done! Keep learning and improving.");
        } else if (score >= 6 && score <= 8) {
            tvResultSubtitle.setText("Awesome effort! Keep sharpening your skills.");
        } else if (score >= 9 && score <= 10) {
            tvResultSubtitle.setText("Outstanding work! You’re a quiz champion! 🏆");
        }

        tvScoreFraction.setText(score + "/" + total);
        tvCorrect.setText(String.valueOf(score));
        tvIncorrect.setText(String.valueOf(total - score));
        tvTime.setText(timeTaken);

        int progress = (total > 0) ? (score * 100) / total : 0;
        progressScore.setProgress(progress);

        // 4. Save Score to Firestore
        saveScoreToFirestore(score, total, difficulty, "Flag Quiz");

        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveScoreToFirestore(int score, int totalQuestions, String difficulty, String gameType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> gameResult = new HashMap<>();
        gameResult.put("userId", user.getUid());
        gameResult.put("difficulty", difficulty);
        gameResult.put("score", score);
        gameResult.put("totalQuestions", totalQuestions);
        gameResult.put("gameType", gameType);
        gameResult.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Scores")
                .add(gameResult)
                .addOnSuccessListener(ref -> {
                    Log.d("FlagResult", "Score saved");
                    // --- ADDED TOAST HERE ---
                    Toast.makeText(FlagResultActivity.this, "Score saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FlagResult", "Error saving", e);
                    Toast.makeText(FlagResultActivity.this, "Failed to save score.", Toast.LENGTH_SHORT).show();
                });
    }
}