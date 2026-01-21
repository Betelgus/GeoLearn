package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;

public class FlashcardSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_selection);

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Connect Buttons to Category Logic
        setupButton(R.id.btnMountains, "mountains");
        setupButton(R.id.btnRivers, "rivers");
        setupButton(R.id.btnDeserts, "deserts");
        setupButton(R.id.btnOceans, "oceans");
        setupButton(R.id.btnWonders, "wonders");
        setupButton(R.id.btnSolarSystem, "solar_system");
        setupButton(R.id.btnContinents, "continents");
        setupButton(R.id.btnIslands, "islands");
        setupButton(R.id.btnSmallestCountries, "smallest_countries");
        setupButton(R.id.btnRandomCountries, "random"); // Calls API
    }

    private void setupButton(int btnId, String categoryKey) {
        findViewById(btnId).setOnClickListener(v -> {
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("CATEGORY", categoryKey);
            startActivity(intent);
        });
    }
}