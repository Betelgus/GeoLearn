package com.example.geolearn.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.geolearn.R;
import com.example.geolearn.api.FlagQuestion;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlagGuessActivity extends AppCompatActivity {

    private static final String TAG = "FlagGuessActivity";
    private static final String PREFS_NAME = "FlagQuizPrefs";
    private static final String KEY_QUESTIONS_UPLOADED = "flag_questions_uploaded_v3"; // Changed to force re-upload

    private ProgressBar progressBar;
    private TextView tvFlagCount;
    private ImageView imgFlag;
    private Button btn1, btn2, btn3, btn4;
    private CountDownTimer timer;

    private List<FlagQuestion> questionList = new ArrayList<>();
    private FlagQuestion currentQuestion;
    private int score = 0;
    private int questionCount = 0;
    private boolean isAnswered = false;
    private long quizStartTime;

    private FirebaseFirestore db;
    private String currentDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_guess);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        uploadFlagQuestionsToFirestoreIfNeeded();

        currentDifficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        if (currentDifficulty == null) currentDifficulty = "Beginner";
        fetchQuestionsFromFirestore(currentDifficulty);
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressFlagTimer);
        tvFlagCount = findViewById(R.id.tvFlagCount);
        imgFlag = findViewById(R.id.imgFlag);
        btn1 = findViewById(R.id.btnFlagOption1);
        btn2 = findViewById(R.id.btnFlagOption2);
        btn3 = findViewById(R.id.btnFlagOption3);
        btn4 = findViewById(R.id.btnFlagOption4);
    }

    private void uploadFlagQuestionsToFirestoreIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean questionsUploaded = prefs.getBoolean(KEY_QUESTIONS_UPLOADED, false);

        if (!questionsUploaded) {
            Log.d(TAG, "Uploading flag questions to Firestore...");
            AssetManager assetManager = getAssets();
            try (InputStream is = assetManager.open("Flag Questions.json");
                 InputStreamReader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<FlagQuestion>>() {}.getType();
                List<FlagQuestion> questions = new Gson().fromJson(reader, listType);

                for (FlagQuestion q : questions) {
                    db.collection("flags").document(q.getId()).set(q)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Flag question " + q.getId() + " uploaded."))
                            .addOnFailureListener(e -> Log.e(TAG, "Error uploading flag question " + q.getId(), e));
                }

                prefs.edit().putBoolean(KEY_QUESTIONS_UPLOADED, true).apply();
                Toast.makeText(this, "Flag question bank updated!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "Error reading/uploading flag questions", e);
            }
        }
    }

    private void fetchQuestionsFromFirestore(String difficulty) {
        db.collection("flags")
                .whereEqualTo("difficulty", difficulty)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(FlagGuessActivity.this, "No flag questions found for difficulty: " + difficulty, Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    List<FlagQuestion> fetchedQuestions = queryDocumentSnapshots.toObjects(FlagQuestion.class);
                    Collections.shuffle(fetchedQuestions);

                    int numberOfQuestions = Math.min(fetchedQuestions.size(), 10);
                    questionList = new ArrayList<>(fetchedQuestions.subList(0, numberOfQuestions));

                    if (questionList.isEmpty()) {
                        Toast.makeText(FlagGuessActivity.this, "Not enough flag questions.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    quizStartTime = System.currentTimeMillis();
                    loadNewRound();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FlagGuessActivity.this, "Error fetching flag questions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void loadNewRound() {
        if (questionCount >= questionList.size()) {
            finishGame();
            return;
        }

        isAnswered = false;
        currentQuestion = questionList.get(questionCount);
        questionCount++;
        tvFlagCount.setText("Flag " + questionCount + "/" + questionList.size());

        Glide.with(this).load(currentQuestion.getFlagUrl()).placeholder(R.drawable.ic_launcher_background).into(imgFlag);

        List<String> options = new ArrayList<>(currentQuestion.getOptions());
        Collections.shuffle(options);

        btn1.setText(options.get(0));
        btn2.setText(options.get(1));
        btn3.setText(options.get(2));
        btn4.setText(options.get(3));

        resetButtons();

        View.OnClickListener listener = v -> checkAnswer((Button) v);
        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);

        startTimer();
    }

    private void checkAnswer(Button selected) {
        if (isAnswered) return;
        isAnswered = true;
        timer.cancel();

        boolean isCorrect = selected.getText().toString().equals(currentQuestion.getCorrectAnswer());
        updateQuestionAnalytics(isCorrect);

        if (isCorrect) {
            score++;
            selected.setBackgroundColor(Color.GREEN);
            selected.setTextColor(Color.WHITE);
        } else {
            selected.setBackgroundColor(Color.RED);
            selected.setTextColor(Color.WHITE);
            highlightCorrect(currentQuestion.getCorrectAnswer());
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::loadNewRound, 1500);
    }

    private void updateQuestionAnalytics(boolean isCorrect) {
        db.collection("flags").document(currentQuestion.getId())
                .update("timesAnswered", FieldValue.increment(1));

        if (isCorrect) {
            db.collection("flags").document(currentQuestion.getId())
                    .update("timesCorrect", FieldValue.increment(1));
        }
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
            public void onFinish() {
                if(!isAnswered) {
                    updateQuestionAnalytics(false);
                    checkAnswer(btn1); // Auto-fail
                }
            }
        }.start();
    }

    private void finishGame() {
        if (timer != null) timer.cancel();

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - quizStartTime;

        int seconds = (int) (timeElapsed / 1000) % 60;
        int minutes = (int) ((timeElapsed / (1000 * 60)) % 60);
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        // MERGE: Point to the unified QuizResultActivity
        Intent intent = new Intent(this, QuizResultActivity.class);

        // Standard Extras
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL_QUESTIONS", questionList.size());
        intent.putExtra("TIME_TAKEN", formattedTime);
        intent.putExtra("GAME_TYPE", "Flag Quiz"); // Explicitly state the game type

        // HAFIZ'S CHANGE: Pass the specific difficulty string
        intent.putExtra("DIFFICULTY", currentDifficulty);

        // BONA'S CHANGE: Pass the Guest Mode flag if it exists
        if (getIntent().getBooleanExtra("IS_GUEST_MODE", false)) {
            intent.putExtra("IS_GUEST_MODE", true);
        }

        startActivity(intent);
        finish();
    }
}
