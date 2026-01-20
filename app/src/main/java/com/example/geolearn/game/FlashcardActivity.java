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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlashcardActivity extends AppCompatActivity {

    private View cardFront, cardBack, cardContainer;
    private ImageView imgFlagFront;
    private TextView tvCountryName, tvCapital, tvRegion;
    private Button btnPrev, btnNext;
    private ImageButton btnBookmark;
    private ImageView btnBack;
    private ProgressBar loadingProgressBar;

    private List<Country> countryList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isBackVisible = false;
    private Set<String> bookmarkedCountries = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

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

        btnBack.setOnClickListener(v -> finish());
        cardContainer.setOnClickListener(v -> flipCard());

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

        btnBookmark.setOnClickListener(v -> toggleBookmark());

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
        if (isBackVisible) {
            cardBack.setVisibility(View.GONE);
            cardFront.setVisibility(View.VISIBLE);
            isBackVisible = false;
        }

        Country current = countryList.get(currentIndex);

        // FIX: Removed R.drawable.ic_launcher_background and used a simple color placeholder
        Glide.with(this)
                .load(current.flags.png)
                .centerCrop()
                .placeholder(android.R.color.darker_gray) // Standard gray placeholder
                .error(android.R.color.holo_red_light)    // Show red if the image fails to load
                .into(imgFlagFront);

        tvCountryName.setText(current.name.common);

        if (current.capital != null && !current.capital.isEmpty()) {
            tvCapital.setText(current.capital.get(0));
        } else {
            tvCapital.setText("N/A");
        }

        tvRegion.setText(current.region);

        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < countryList.size() - 1);
        btnPrev.setAlpha(currentIndex > 0 ? 1.0f : 0.5f);
        btnNext.setAlpha(currentIndex < countryList.size() - 1 ? 1.0f : 0.5f);

        updateBookmarkIcon();
    }

    private void flipCard() {
        if (isBackVisible) {
            cardBack.setVisibility(View.GONE);
            cardFront.setVisibility(View.VISIBLE);
        } else {
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
        } else {
            bookmarkedCountries.add(countryName);
        }
        updateBookmarkIcon();
    }

    private void updateBookmarkIcon() {
        if (countryList.isEmpty()) return;
        String countryName = countryList.get(currentIndex).name.common;
        if (bookmarkedCountries.contains(countryName)) {
            // Ensure you have these drawables in your res/drawable folder
            btnBookmark.setImageResource(R.drawable.ic_star_filled);
        } else {
            btnBookmark.setImageResource(R.drawable.ic_star_outline);
        }
    }
}