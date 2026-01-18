package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;
import com.example.geolearn.home.MainMenuActivity;

public class FlagResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_result);

        // 1. Initialize Views
        TextView tvScoreFraction = findViewById(R.id.tvScoreFraction);
        TextView tvCorrect = findViewById(R.id.tvCorrect);
        TextView tvIncorrect = findViewById(R.id.tvIncorrect);
        TextView tvTime = findViewById(R.id.tvTime);
        ProgressBar progressScore = findViewById(R.id.progressScore);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // 2. Get Data from Intent
        // If "SCORE" is missing, default to 0. If "TOTAL" missing, default to 10.
        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL_QUESTIONS", 10);
        String timeTaken = getIntent().getStringExtra("TIME_TAKEN");

        if (timeTaken == null) timeTaken = "--:--";

        // 3. Calculate Stats (NO HARDCODING)
        int correct = score;
        int incorrect = total - score;

        // 4. Set Data
        tvScoreFraction.setText(score + "/" + total);
        tvCorrect.setText(String.valueOf(correct));   // Shows actual correct count
        tvIncorrect.setText(String.valueOf(incorrect)); // Shows actual incorrect count
        tvTime.setText(timeTaken);

        // Update progress bar
        int progress = 0;
        if (total > 0) {
            progress = (score * 100) / total;
        }
        progressScore.setProgress(progress);

        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}