package com.example.geolearn.game;

import android.os.Bundle;
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
import com.example.geolearn.api.Geoapi;

import java.util.ArrayList;
import java.util.Collections; // Import this for shuffling
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlashcardActivity extends AppCompatActivity {

    // Views
    private View cardFront, cardBack, cardContainer;
    private ImageView imgFlagFront;
    private TextView tvCountryName, tvCapital, tvRegion;
    private Button btnPrev, btnNext;
    private ImageButton btnBookmark;
    private ImageView btnBack;
    private ProgressBar loadingProgressBar;

    // Data
    private List<Country> countryList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isBackVisible = false;

    // Bookmarks
    private Set<String> bookmarkedCountries = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // 1. Initialize Views
        btnBack = findViewById(R.id.btnBack);
        cardContainer = findViewById(R.id.cardContainer);
        cardFront = findViewById(R.id.cardFront);
        cardBack = findViewById(R.id.cardBack);

        imgFlagFront = findViewById(R.id.imgFlagFront);
        tvCountryName = findViewById(R.id.tvCountryName);
        tvCapital = findViewById(R.id.tvCapital);
        tvRegion = findViewById(R.id.tvRegion);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBookmark = findViewById(R.id.btnBookmark);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // 2. Back Button Logic
        btnBack.setOnClickListener(v -> finish());

        // 3. Card Flip Listener
        cardContainer.setOnClickListener(v -> flipCard());

        // 4. Navigation Listeners
        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateCardUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < countryList.size() - 1) {
                currentIndex++;
                updateCardUI();
            }
        });

        // 5. Bookmark Listener
        btnBookmark.setOnClickListener(v -> toggleBookmark());

        // 6. Fetch Data
        fetchCountries();
    }

    private void fetchCountries() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        cardContainer.setVisibility(View.INVISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://restcountries.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.create(Geoapi.class).getCountries().enqueue(new Callback<List<Country>>() {
            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                loadingProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    countryList = response.body();

                    // --- NEW: Shuffle the list to randomize order ---
                    Collections.shuffle(countryList);

                    if (!countryList.isEmpty()) {
                        cardContainer.setVisibility(View.VISIBLE);
                        updateCardUI();
                    }
                } else {
                    Toast.makeText(FlashcardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(FlashcardActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCardUI() {
        // Always reset to Front side when changing cards
        if (isBackVisible) {
            cardBack.setVisibility(View.GONE);
            cardFront.setVisibility(View.VISIBLE);
            isBackVisible = false;
        }

        Country current = countryList.get(currentIndex);

        // Update Front (Flag)
        Glide.with(this)
                .load(current.flags.png)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgFlagFront);

        // Update Back (Details)
        tvCountryName.setText(current.name.common);

        if (current.capital != null && !current.capital.isEmpty()) {
            tvCapital.setText(current.capital.get(0));
        } else {
            tvCapital.setText("N/A");
        }

        tvRegion.setText(current.region);

        // Update Navigation Buttons State
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < countryList.size() - 1);

        btnPrev.setAlpha(currentIndex > 0 ? 1.0f : 0.5f);
        btnNext.setAlpha(currentIndex < countryList.size() - 1 ? 1.0f : 0.5f);

        // Update Bookmark Icon
        updateBookmarkIcon();
    }

    private void flipCard() {
        if (isBackVisible) {
            // Show Front
            cardBack.setVisibility(View.GONE);
            cardFront.setVisibility(View.VISIBLE);
        } else {
            // Show Back
            cardFront.setVisibility(View.GONE);
            cardBack.setVisibility(View.VISIBLE);
        }
        isBackVisible = !isBackVisible;
    }

    private void toggleBookmark() {
        if (countryList.isEmpty()) return;

        String countryName = countryList.get(currentIndex).name.common;
        if (bookmarkedCountries.contains(countryName)) {
            bookmarkedCountries.remove(countryName);
            Toast.makeText(this, "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
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
            btnBookmark.setColorFilter(null);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_star_outline);
            btnBookmark.setColorFilter(null);
        }
    }
}