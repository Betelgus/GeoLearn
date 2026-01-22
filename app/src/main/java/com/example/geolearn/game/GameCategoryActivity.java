package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;

public class GameCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_category);

        // 1. Get the difficulty passed from Main Menu
        String difficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");

        // 2. Display it
        TextView tvDifficulty = findViewById(R.id.tvSelectedDifficulty);
        tvDifficulty.setText("Difficulty: " + difficulty);

        // 3. Handle Trivia Mode Click (Goes to existing QuizUIActivity)
        findViewById(R.id.cardModeTrivia).setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizUIActivity.class);
            intent.putExtra("DIFFICULTY_LEVEL", difficulty);
            if (getIntent().getBooleanExtra("IS_GUEST_MODE", false)) {
                intent.putExtra("IS_GUEST_MODE", true);
            }
            startActivity(intent);
        });

        // 4. Handle Flag Mode Click (Goes to NEW FlagGuessActivity)
        findViewById(R.id.cardModeFlag).setOnClickListener(v -> {
            Intent intent = new Intent(this, FlagGuessActivity.class);
            intent.putExtra("DIFFICULTY_LEVEL", difficulty);
            if (getIntent().getBooleanExtra("IS_GUEST_MODE", false)) {
                intent.putExtra("IS_GUEST_MODE", true);
            }
            startActivity(intent);
        });

    }
}