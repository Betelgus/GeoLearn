package com.example.geolearn.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.geolearn.R;
import com.example.geolearn.api.Country;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlashcardActivity extends AppCompatActivity {

    private ImageView imgFlag;
    private TextView tvCountryName, tvCapital, tvRegion;
    private Button btnPrev, btnNext;
    private ImageButton btnBookmark;
    private ProgressBar loadingProgressBar;

    private List<Country> countryList = new ArrayList<>();
    private int currentIndex = 0;
    private Set<String> bookmarkedCountries = new HashSet<>();
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // Get category from intent, default to "mountains" if null
        category = getIntent().getStringExtra("CATEGORY");
        if (category == null) category = "mountains";

        initViews();
        loadData();
    }

    private void initViews() {
        imgFlag = findViewById(R.id.imgFlag);
        tvCountryName = findViewById(R.id.tvCountryName);
        tvCapital = findViewById(R.id.tvCapital);
        tvRegion = findViewById(R.id.tvRegion);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBookmark = findViewById(R.id.btnBookmark);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Previous button click
        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                showCard();
            }
        });

        // Next button click
        btnNext.setOnClickListener(v -> {
            if (currentIndex < countryList.size() - 1) {
                currentIndex++;
                showCard();
            }
        });

        // Bookmark button click
        btnBookmark.setOnClickListener(v -> toggleBookmark());
    }

    private void loadData() {
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

        // Fetch data from local JSON via FlashcardData helper class
        countryList = FlashcardData.getCategoryData(this, category);

        if (countryList != null && !countryList.isEmpty()) {
            showCard();
        } else {
            Toast.makeText(this, "No items found for category: " + category, Toast.LENGTH_SHORT).show();
        }
    }

    private void showCard() {
        if (countryList.isEmpty()) return;

        Country country = countryList.get(currentIndex);

        // 1. Set Texts
        if (country.name != null) tvCountryName.setText(country.name.common);
        else tvCountryName.setText("Unknown");

        if (country.capital != null && !country.capital.isEmpty()) {
            tvCapital.setText(country.capital.get(0));
        } else {
            tvCapital.setText("N/A");
        }

        tvRegion.setText(country.region);

        // 2. Load Local Image (Smart Loader)
        if (country.flags != null && country.flags.png != null) {
            String imageName = country.flags.png;

            // Remove extension if present in JSON (e.g. "everest.jpg" -> "everest")
            if (imageName.contains(".")) {
                imageName = imageName.substring(0, imageName.lastIndexOf('.'));
            }

            // Android resources must be lowercase and have no spaces
            imageName = imageName.toLowerCase().replace(" ", "_");

            // Look up the Resource ID by name in the drawable folder
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());

            if (resId != 0) {
                // Image found! Load it.
                Glide.with(this)
                        .load(resId)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imgFlag);
            } else {
                // Image NOT found in drawable folder
                Log.e("FlashcardActivity", "Image not found: " + imageName);
                imgFlag.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgFlag.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 3. Update Buttons State
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < countryList.size() - 1);
        btnPrev.setAlpha(currentIndex > 0 ? 1.0f : 0.5f);
        btnNext.setAlpha(currentIndex < countryList.size() - 1 ? 1.0f : 0.5f);

        updateBookmarkIcon();
    }

    private void toggleBookmark() {
        if (countryList.isEmpty()) return;
        String countryName = countryList.get(currentIndex).name.common;

        if (bookmarkedCountries.contains(countryName)) {
            bookmarkedCountries.remove(countryName);
            Toast.makeText(this, "Removed from bookmarks", Toast.LENGTH_SHORT).show();
        } else {
            bookmarkedCountries.add(countryName);
            Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show();
        }
        updateBookmarkIcon();
    }

    private void updateBookmarkIcon() {
        if (countryList.isEmpty()) return;
        String countryName = countryList.get(currentIndex).name.common;

        if (bookmarkedCountries.contains(countryName)) {
            btnBookmark.setImageResource(R.drawable.ic_star_filled);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_star_outline);
        }
    }
}