package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.example.geolearn.home.MainMenuActivity;

public class QuizResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // 1. Initialize Views
        TextView tvScoreFraction = findViewById(R.id.tvScoreFraction);
        TextView tvResultSubtitle = findViewById(R.id.tvResultSubtitle); // Link to XML ID
        TextView tvCorrect = findViewById(R.id.tvCorrect);
        TextView tvIncorrect = findViewById(R.id.tvIncorrect);
        TextView tvTime = findViewById(R.id.tvTime);
        ProgressBar progressScore = findViewById(R.id.progressScore);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // 2. Get Data from Intent
        int score = getIntent().getIntExtra("SCORE", 0);
        int totalQuestions = getIntent().getIntExtra("TOTAL_QUESTIONS", 10);
        String timeTaken = getIntent().getStringExtra("TIME_TAKEN");

        if (timeTaken == null) timeTaken = "--:--";

        // 3. SET THE WORDING BASED ON SCORE
        if (score >= 0 && score <= 2) {
            tvResultSubtitle.setText("Good try! Every mistake helps you learn more.");
        } else if (score >= 3 && score <= 5) {
            tvResultSubtitle.setText("Good job! You’re getting there — keep going!");
        } else if (score >= 6 && score <= 8) {
            tvResultSubtitle.setText("Well done! Your hard work is paying off.");
        } else if (score >= 9 && score <= 10) {
            tvResultSubtitle.setText("Excellent! You nailed the quiz! 🎉");
        }

        // 4. Set Data to Views
        tvScoreFraction.setText(score + "/" + totalQuestions);
        tvCorrect.setText(String.valueOf(score));
        tvIncorrect.setText(String.valueOf(totalQuestions - score));
        tvTime.setText(timeTaken);

        // 5. Update Circular Progress Bar
        int percentage = 0;
        if (totalQuestions > 0) {
            percentage = (score * 100) / totalQuestions;
        }
        progressScore.setProgress(percentage);

        // 6. Back to Menu
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}