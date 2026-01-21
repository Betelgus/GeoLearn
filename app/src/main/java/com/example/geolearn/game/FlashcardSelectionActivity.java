package com.example.geolearn.game;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;

public class FlashcardSelectionActivity extends AppCompatActivity {

    private boolean forceGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_selection);

        // 1. Receive the Force Guest flag from the Main Menu
        forceGuest = getIntent().getBooleanExtra("FORCE_GUEST", false);

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Connect Buttons
        setupButton(R.id.btnMountains, "mountains");
        setupButton(R.id.btnRivers, "rivers");
        setupButton(R.id.btnDeserts, "deserts");
        setupButton(R.id.btnOceans, "oceans");
        setupButton(R.id.btnWonders, "wonders");
        setupButton(R.id.btnSolarSystem, "solar_system");
        setupButton(R.id.btnContinents, "continents");
        setupButton(R.id.btnIslands, "islands");
        setupButton(R.id.btnSmallestCountries, "smallest_countries");

        // Ensure you have a button for random or remove this line if not
        if (findViewById(R.id.btnRandomCountries) != null) {
            setupButton(R.id.btnRandomCountries, "random");
        }
    }

    private void setupButton(int btnId, String categoryKey) {
        findViewById(btnId).setOnClickListener(v -> {
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("CATEGORY", categoryKey);

            // 2. PASS IT FORWARD: Send the flag to the actual game screen
            intent.putExtra("FORCE_GUEST", forceGuest);

            startActivity(intent);
        });
    }
}