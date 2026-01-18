package com.example.geolearn.game;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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
    private Button btn1, btn2, btn3, btn4;
    private CountDownTimer timer;

    private List<Country> allCountries;
    private Country currentCountry;
    private int score = 0; // This records the CORRECT answers
    private int questionCount = 0;
    private boolean isAnswered = false;

    // --- NEW: Time Tracking ---
    private long quizStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_guess);

        progressBar = findViewById(R.id.progressFlagTimer);
        tvFlagCount = findViewById(R.id.tvFlagCount);
        imgFlag = findViewById(R.id.imgFlag);
        btn1 = findViewById(R.id.btnFlagOption1);
        btn2 = findViewById(R.id.btnFlagOption2);
        btn3 = findViewById(R.id.btnFlagOption3);
        btn4 = findViewById(R.id.btnFlagOption4);

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

                    // --- START CLOCK HERE ---
                    quizStartTime = System.currentTimeMillis();

                    loadNewRound();
                }
            }
            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                Toast.makeText(FlagGuessActivity.this, "Check Internet Connection", Toast.LENGTH_SHORT).show();
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

        // Pick Random Country
        Random random = new Random();
        currentCountry = allCountries.get(random.nextInt(allCountries.size()));

        // Pick Wrong Options
        List<String> options = new ArrayList<>();
        options.add(currentCountry.name.common);
        while (options.size() < 4) {
            Country wrong = allCountries.get(random.nextInt(allCountries.size()));
            if (!options.contains(wrong.name.common)) options.add(wrong.name.common);
        }
        Collections.shuffle(options);

        // Load Image
        Glide.with(this).load(currentCountry.flags.png).placeholder(R.drawable.ic_launcher_background).into(imgFlag);

        // Set Buttons
        btn1.setText(options.get(0)); btn2.setText(options.get(1));
        btn3.setText(options.get(2)); btn4.setText(options.get(3));

        resetButtons();

        View.OnClickListener listener = v -> checkAnswer((Button) v);
        btn1.setOnClickListener(listener); btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener); btn4.setOnClickListener(listener);

        startTimer();
    }

    private void checkAnswer(Button selected) {
        if (isAnswered) return;
        isAnswered = true;
        timer.cancel();

        if (selected.getText().toString().equals(currentCountry.name.common)) {
            score++; // Increment CORRECT count
            selected.setBackgroundColor(Color.GREEN);
            selected.setTextColor(Color.WHITE);
        } else {
            // Incorrect answer
            selected.setBackgroundColor(Color.RED);
            selected.setTextColor(Color.WHITE);
            highlightCorrect(currentCountry.name.common);
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::loadNewRound, 1500);
    }

    private void highlightCorrect(String name) {
        if (btn1.getText().equals(name)) btn1.setBackgroundColor(Color.GREEN);
        if (btn2.getText().equals(name)) btn2.setBackgroundColor(Color.GREEN);
        if (btn3.getText().equals(name)) btn3.setBackgroundColor(Color.GREEN);
        if (btn4.getText().equals(name)) btn4.setBackgroundColor(Color.GREEN);
    }

    private void resetButtons() {
        int color = getColor(R.color.text_primary);
        btn1.setBackgroundColor(Color.TRANSPARENT); btn1.setTextColor(color);
        btn2.setBackgroundColor(Color.TRANSPARENT); btn2.setTextColor(color);
        btn3.setBackgroundColor(Color.TRANSPARENT); btn3.setTextColor(color);
        btn4.setBackgroundColor(Color.TRANSPARENT); btn4.setTextColor(color);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        progressBar.setProgress(100);
        timer = new CountDownTimer(15000, 100) {
            public void onTick(long millis) { progressBar.setProgress((int)(millis/150)); }
            public void onFinish() { if(!isAnswered) checkAnswer(btn1); /*Auto fail*/ }
        }.start();
    }

    private void finishGame() {
        if (timer != null) timer.cancel();

        // Calculate Time
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - quizStartTime;

        int seconds = (int) (timeElapsed / 1000) % 60;
        int minutes = (int) ((timeElapsed / (1000 * 60)) % 60);
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        Intent intent = new Intent(this, FlagResultActivity.class);
        intent.putExtra("SCORE", score); // Pass Correct count
        intent.putExtra("TOTAL_QUESTIONS", 10); // Pass Total count
        intent.putExtra("TIME_TAKEN", formattedTime); // Pass Time

        startActivity(intent);
        finish();
    }
}