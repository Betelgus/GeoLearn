package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;
import com.example.geolearn.home.GuestMainMenuActivity;
import com.example.geolearn.home.MainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class QuizResultActivity extends AppCompatActivity {

    private static final String TAG = "QuizResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // UI Initialization
        TextView tvScoreFraction = findViewById(R.id.tvScoreFraction);
        TextView tvCorrect = findViewById(R.id.tvCorrect);
        TextView tvIncorrect = findViewById(R.id.tvIncorrect);
        TextView tvTime = findViewById(R.id.tvTime);
        ProgressBar progressScore = findViewById(R.id.progressScore);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // 1. GET DATA (Merged from both)
        int score = getIntent().getIntExtra("SCORE", 0);
        int totalQuestions = getIntent().getIntExtra("TOTAL_QUESTIONS", 10);
        String timeTaken = getIntent().getStringExtra("TIME_TAKEN");

        // Hafiz's Data: Difficulty & Game Type
        String difficulty = getIntent().getStringExtra("DIFFICULTY");
        if (difficulty == null) difficulty = "Beginner"; // Default
        String gameType = getIntent().getStringExtra("GAME_TYPE");
        if (gameType == null) gameType = "Trivia Quiz"; // Default

        // Bona's Data: Guest Mode Flag
        boolean isGuest = getIntent().getBooleanExtra("IS_GUEST_MODE", false);

        if (timeTaken == null) timeTaken = "--:--";

        // 2. SETUP UI
        int correct = score;
        int incorrect = totalQuestions - score;
        tvScoreFraction.setText(score + "/" + totalQuestions);
        tvCorrect.setText(String.valueOf(correct));
        tvIncorrect.setText(String.valueOf(incorrect));
        tvTime.setText(timeTaken);

        int percentage = 0;
        if (totalQuestions > 0) {
            percentage = (score * 100) / totalQuestions;
        }
        progressScore.setProgress(percentage);

        // 3. SAVE LOGIC (Merged)
        saveScoreToFirestore(score, totalQuestions, timeTaken, difficulty, gameType, isGuest);

        // 4. NAVIGATION LOGIC (Bona's Logic for Guest routing)
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent;
            if (isGuest) {
                // Return to Guest Menu
                intent = new Intent(QuizResultActivity.this, GuestMainMenuActivity.class);
            } else {
                // Return to Standard Menu
                intent = new Intent(QuizResultActivity.this, MainMenuActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Merged Save Function
    private void saveScoreToFirestore(int score, int totalQuestions, String timeTaken,
                                      String difficulty, String gameType, boolean isGuest) {

        if (isGuest) {
            Log.d(TAG, "Guest mode active. Skipping Firestore save.");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> quizResult = new HashMap<>();
            quizResult.put("userId", userId);
            quizResult.put("difficulty", difficulty);
            quizResult.put("gameType", gameType);
            quizResult.put("score", score);
            quizResult.put("totalQuestions", totalQuestions);
            quizResult.put("timeTaken", timeTaken);
            quizResult.put("timestamp", FieldValue.serverTimestamp());

            db.collection("Scores")
                    .add(quizResult)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Score saved with ID: " + documentReference.getId());
                        Toast.makeText(QuizResultActivity.this, "Score saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error saving score", e);
                        Toast.makeText(QuizResultActivity.this, "Failed to save score.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d(TAG, "User not logged in. Score not saved.");
        }
    }
}