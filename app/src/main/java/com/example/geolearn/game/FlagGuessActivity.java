package com.example.geolearn.game;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.example.geolearn.R;
import com.example.geolearn.api.Country;
import com.example.geolearn.api.Geoapi;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlagGuessActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvFlagCount;
    private ImageView imgFlag;
    private MaterialButton[] optionButtons;
    private CountDownTimer timer;

    private List<Country> allCountries;
    private Country currentCountry;
    private int score = 0;
    private int questionCount = 0;
    private boolean isAnswered = false;
    private long quizStartTime;

    // Create a factory for the crossfade animation to avoid the "symbol not found" error
    private final DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_guess);

        progressBar = findViewById(R.id.progressFlagTimer);
        tvFlagCount = findViewById(R.id.tvFlagCount);
        imgFlag = findViewById(R.id.imgFlag);

        optionButtons = new MaterialButton[]{
                findViewById(R.id.btnFlagOption1),
                findViewById(R.id.btnFlagOption2),
                findViewById(R.id.btnFlagOption3),
                findViewById(R.id.btnFlagOption4)
        };

        fetchCountries();
    }

    private void fetchCountries() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://restcountries.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.create(Geoapi.class).getCountries().enqueue(new Callback<List<Country>>() {
            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCountries = response.body();
                    quizStartTime = System.currentTimeMillis();
                    loadNewRound();
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                Toast.makeText(FlagGuessActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadNewRound() {
        if (questionCount >= 10) {
            finishGame();
            return;
        }
        questionCount++;
        isAnswered = false;
        tvFlagCount.setText("Flag " + questionCount + "/10");

        Random random = new Random();
        currentCountry = allCountries.get(random.nextInt(allCountries.size()));

        List<String> options = new ArrayList<>();
        options.add(currentCountry.name.common);
        while (options.size() < 4) {
            Country wrong = allCountries.get(random.nextInt(allCountries.size()));
            if (!options.contains(wrong.name.common)) options.add(wrong.name.common);
        }
        Collections.shuffle(options);

        // This approach uses the factory to ensure it compiles correctly
        Glide.with(this)
                .load(currentCountry.flags.png)
                .transition(DrawableTransitionOptions.with(factory))
                .placeholder(android.R.color.transparent)
                .into(imgFlag);

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
            resetButtonStyle(optionButtons[i]);
            optionButtons[i].setOnClickListener(v -> checkAnswer((MaterialButton) v));
        }

        startTimer();
    }

    private void checkAnswer(MaterialButton selected) {
        if (isAnswered) return;
        isAnswered = true;
        if (timer != null) timer.cancel();

        String selectedText = selected.getText().toString();
        String correctText = currentCountry.name.common;

        if (selectedText.equals(correctText)) {
            score++;
            applyColor(selected, Color.GREEN);
        } else {
            applyColor(selected, Color.RED);
            highlightCorrect(correctText);
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::loadNewRound, 1500);
    }

    private void highlightCorrect(String name) {
        for (MaterialButton btn : optionButtons) {
            if (btn.getText().toString().equals(name)) {
                applyColor(btn, Color.GREEN);
            }
        }
    }

    private void applyColor(MaterialButton btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
        btn.setTextColor(Color.WHITE);
        btn.setStrokeColor(ColorStateList.valueOf(color));
    }

    private void resetButtonStyle(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btn.setTextColor(getColor(R.color.text_primary));
        btn.setStrokeColor(ColorStateList.valueOf(getColor(R.color.secondary)));
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        progressBar.setProgress(100);
        timer = new CountDownTimer(15000, 100) {
            public void onTick(long millis) { progressBar.setProgress((int) (millis / 150)); }
            public void onFinish() { if (!isAnswered) checkAnswer(optionButtons[0]); }
        }.start();
    }

    private void finishGame() {
        if (timer != null) timer.cancel();
        long timeElapsed = System.currentTimeMillis() - quizStartTime;
        String formattedTime = String.format("%02d:%02d", (timeElapsed / 60000), (timeElapsed / 1000) % 60);

        Intent intent = new Intent(this, FlagResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL_QUESTIONS", 10);
        intent.putExtra("TIME_TAKEN", formattedTime);
        startActivity(intent);
        finish();
    }
}