package com.example.geolearn.game;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.example.geolearn.R;
import com.example.geolearn.api.Geoapi;
import com.example.geolearn.api.TriviaResponse;
import com.example.geolearn.api.TriviaResponse.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizUIActivity extends AppCompatActivity {

    // UI Components
    private ProgressBar progressBar;
    private TextView tvQuestion, tvQuestionCount;
    private Button btn1, btn2, btn3, btn4;
    private LinearLayout loadingLayout;
    private Group quizContentGroup;

    // Game Logic Variables
    private List<Question> questionList = new ArrayList<>();
    private int index = 0;
    private int score = 0;
    private CountDownTimer timer;
    private boolean isAnswered = false;

    // --- NEW: Time Tracking Variable ---
    private long quizStartTime;

    // Default time per question: 15 seconds
    private static final long TOTAL_TIME = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_ui);

        // 1. Initialize Views
        progressBar = findViewById(R.id.progressBarTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        loadingLayout = findViewById(R.id.loadingLayout);
        quizContentGroup = findViewById(R.id.quizContentGroup);

        btn1 = findViewById(R.id.btnOption1);
        btn2 = findViewById(R.id.btnOption2);
        btn3 = findViewById(R.id.btnOption3);
        btn4 = findViewById(R.id.btnOption4);

        // 2. Fetch Questions
        String difficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        if(difficulty == null) difficulty = "medium";

        fetchQuestions(difficulty.toLowerCase());

        // 3. Set Button Click Listeners
        View.OnClickListener answerListener = view -> {
            if (!isAnswered) {
                checkAnswer((Button) view);
            }
        };

        btn1.setOnClickListener(answerListener);
        btn2.setOnClickListener(answerListener);
        btn3.setOnClickListener(answerListener);
        btn4.setOnClickListener(answerListener);
    }

    // Variable to track retries if needed
    private int retryCount = 0;

    private void fetchQuestions(String difficulty) {
        // Ensure loading screen is visible
        if(loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
        if(quizContentGroup != null) quizContentGroup.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Geoapi api = retrofit.create(Geoapi.class);

        api.getQuestions(10, 22, difficulty, "multiple").enqueue(new Callback<TriviaResponse>() {
            @Override
            public void onResponse(Call<TriviaResponse> call, Response<TriviaResponse> response) {
                // --- HANDLE RATE LIMIT (429) ---
                if (response.code() == 429) {
                    Log.d("Quiz", "Rate limit (429). Retrying in 5 seconds...");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        fetchQuestions(difficulty); // Try again
                    }, 5000);
                    return;
                }

                // --- HANDLE SUCCESS ---
                if (response.isSuccessful() && response.body() != null) {
                    List<Question> results = response.body().getResults();

                    if (results != null && !results.isEmpty()) {
                        questionList = results;

                        // Hide loader, Show Game
                        if(loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                        if(quizContentGroup != null) quizContentGroup.setVisibility(View.VISIBLE);

                        // --- START THE CLOCK HERE ---
                        quizStartTime = System.currentTimeMillis();

                        loadQuestion();
                    } else {
                        // Retry with ANY difficulty if specific one fails
                        if (difficulty != null) {
                            fetchQuestions(null);
                        } else {
                            Toast.makeText(QuizUIActivity.this, "No Geography questions found.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                } else {
                    Toast.makeText(QuizUIActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TriviaResponse> call, Throwable t) {
                Toast.makeText(QuizUIActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadQuestion() {
        if (index >= questionList.size()) {
            finishGame();
            return;
        }

        // Reset State
        isAnswered = false;
        resetButtonColors();

        // Update UI
        Question q = questionList.get(index);
        tvQuestionCount.setText("Question " + (index + 1) + "/" + questionList.size());

        // Handle HTML characters
        tvQuestion.setText(Html.fromHtml(q.getQuestion(), Html.FROM_HTML_MODE_LEGACY));

        // Shuffle Answers
        List<String> answers = new ArrayList<>(q.getIncorrectAnswers());
        answers.add(q.getCorrectAnswer());
        Collections.shuffle(answers);

        // Safety check to ensure we have 4 answers
        if(answers.size() >= 4) {
            btn1.setText(Html.fromHtml(answers.get(0), Html.FROM_HTML_MODE_LEGACY));
            btn2.setText(Html.fromHtml(answers.get(1), Html.FROM_HTML_MODE_LEGACY));
            btn3.setText(Html.fromHtml(answers.get(2), Html.FROM_HTML_MODE_LEGACY));
            btn4.setText(Html.fromHtml(answers.get(3), Html.FROM_HTML_MODE_LEGACY));

            btn1.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.VISIBLE);
            btn3.setVisibility(View.VISIBLE);
            btn4.setVisibility(View.VISIBLE);
        } else {
            // Handle True/False questions gracefully
            btn1.setText(Html.fromHtml(answers.get(0), Html.FROM_HTML_MODE_LEGACY));
            btn2.setText(Html.fromHtml(answers.get(1), Html.FROM_HTML_MODE_LEGACY));
            btn3.setVisibility(View.INVISIBLE);
            btn4.setVisibility(View.INVISIBLE);
        }

        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        progressBar.setProgress(100);

        timer = new CountDownTimer(TOTAL_TIME, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished * 100 / TOTAL_TIME);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                if (!isAnswered) {
                    showCorrectAnswer();
                    isAnswered = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        index++;
                        loadQuestion();
                    }, 1500);
                }
            }
        }.start();
    }

    private void checkAnswer(Button selectedBtn) {
        isAnswered = true;
        if (timer != null) timer.cancel();

        String selectedText = selectedBtn.getText().toString();
        String correctText = Html.fromHtml(questionList.get(index).getCorrectAnswer(), Html.FROM_HTML_MODE_LEGACY).toString();

        if (selectedText.equals(correctText)) {
            score++;
            selectedBtn.setBackgroundColor(Color.GREEN);
            selectedBtn.setTextColor(Color.WHITE);
        } else {
            selectedBtn.setBackgroundColor(Color.RED);
            selectedBtn.setTextColor(Color.WHITE);
            showCorrectAnswer();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            index++;
            loadQuestion();
        }, 1500);
    }

    private void showCorrectAnswer() {
        String correctText = Html.fromHtml(questionList.get(index).getCorrectAnswer(), Html.FROM_HTML_MODE_LEGACY).toString();

        if (btn1.getText().toString().equals(correctText)) btn1.setBackgroundColor(Color.GREEN);
        if (btn2.getText().toString().equals(correctText)) btn2.setBackgroundColor(Color.GREEN);
        if (btn3.getText().toString().equals(correctText)) btn3.setBackgroundColor(Color.GREEN);
        if (btn4.getText().toString().equals(correctText)) btn4.setBackgroundColor(Color.GREEN);
    }

    private void resetButtonColors() {
        int defaultColor = getColor(R.color.text_primary);

        btn1.setBackgroundColor(Color.TRANSPARENT); btn1.setTextColor(defaultColor);
        btn2.setBackgroundColor(Color.TRANSPARENT); btn2.setTextColor(defaultColor);
        btn3.setBackgroundColor(Color.TRANSPARENT); btn3.setTextColor(defaultColor);
        btn4.setBackgroundColor(Color.TRANSPARENT); btn4.setTextColor(defaultColor);
    }

    private void finishGame() {
        if (timer != null) timer.cancel();

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - quizStartTime;

        int seconds = (int) (timeElapsed / 1000) % 60;
        int minutes = (int) ((timeElapsed / (1000 * 60)) % 60);
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL_QUESTIONS", questionList.size());
        intent.putExtra("TIME_TAKEN", formattedTime);
        intent.putExtra("QUIZ_TYPE", "trivia"); // Add this line

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}