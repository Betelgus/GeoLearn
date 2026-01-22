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
import com.example.geolearn.api.Question;
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

public class QuizUIActivity extends AppCompatActivity {

    private static final String TAG = "QuizUIActivity";
    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_QUESTIONS_UPLOADED = "questions_uploaded_v2"; // Changed to force re-upload

    private ProgressBar progressBar;
    private TextView tvQuestion, tvQuestionCount;
    private Button btn1, btn2, btn3, btn4;
    private LinearLayout loadingLayout;
    private Group quizContentGroup;

    private List<Question> questionList = new ArrayList<>();
    private int index = 0;
    private int score = 0;
    private CountDownTimer timer;
    private boolean isAnswered = false;
    private long quizStartTime;
    private static final long TOTAL_TIME = 15000;

    private FirebaseFirestore db;
    private String currentDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_ui);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        uploadQuestionsToFirestoreIfNeeded();

        currentDifficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        if (currentDifficulty == null) currentDifficulty = "beginner";
        fetchQuestionsFromFirestore(currentDifficulty.toLowerCase());

        setupButtonClickListeners();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBarTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        loadingLayout = findViewById(R.id.loadingLayout);
        quizContentGroup = findViewById(R.id.quizContentGroup);
        btn1 = findViewById(R.id.btnOption1);
        btn2 = findViewById(R.id.btnOption2);
        btn3 = findViewById(R.id.btnOption3);
        btn4 = findViewById(R.id.btnOption4);
    }

    private void setupButtonClickListeners() {
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

    private void uploadQuestionsToFirestoreIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean questionsUploaded = prefs.getBoolean(KEY_QUESTIONS_UPLOADED, false);

        if (!questionsUploaded) {
            Log.d(TAG, "First time setup: Uploading questions to Firestore...");
            AssetManager assetManager = getAssets();
            try (InputStream is = assetManager.open("Trivia Questions.json");
                 InputStreamReader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<Question>>() {}.getType();
                List<Question> questions = new Gson().fromJson(reader, listType);

                for (Question q : questions) {
                    db.collection("questions").document(q.getId()).set(q)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Question " + q.getId() + " uploaded."))
                            .addOnFailureListener(e -> Log.e(TAG, "Error uploading question " + q.getId(), e));
                }

                prefs.edit().putBoolean(KEY_QUESTIONS_UPLOADED, true).apply();
                Toast.makeText(this, "Question bank initialized!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "Error reading or uploading questions from assets", e);
                Toast.makeText(this, "Error setting up question bank.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchQuestionsFromFirestore(String difficulty) {
        loadingLayout.setVisibility(View.VISIBLE);
        quizContentGroup.setVisibility(View.GONE);

        db.collection("questions")
                .whereEqualTo("difficulty", difficulty)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(QuizUIActivity.this, "No questions found for difficulty: " + difficulty, Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    List<Question> fetchedQuestions = queryDocumentSnapshots.toObjects(Question.class);
                    Collections.shuffle(fetchedQuestions);

                    int numberOfQuestions = Math.min(fetchedQuestions.size(), 10);
                    questionList = new ArrayList<>(fetchedQuestions.subList(0, numberOfQuestions));

                    if (questionList.isEmpty()) {
                        Toast.makeText(QuizUIActivity.this, "Not enough questions to start quiz.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    loadingLayout.setVisibility(View.GONE);
                    quizContentGroup.setVisibility(View.VISIBLE);
                    quizStartTime = System.currentTimeMillis();
                    loadQuestion();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(QuizUIActivity.this, "Error fetching questions: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error fetching questions from Firestore", e);
                    finish();
                });
    }

    private void loadQuestion() {
        if (index >= questionList.size()) {
            finishGame();
            return;
        }

        isAnswered = false;
        resetButtonColors();

        Question q = questionList.get(index);
        tvQuestionCount.setText("Question " + (index + 1) + "/" + questionList.size());
        tvQuestion.setText(Html.fromHtml(q.getQuestionText(), Html.FROM_HTML_MODE_LEGACY));

        List<String> options = new ArrayList<>(q.getOptions());
        Collections.shuffle(options);

        btn1.setText(Html.fromHtml(options.get(0), Html.FROM_HTML_MODE_LEGACY));
        btn2.setText(Html.fromHtml(options.get(1), Html.FROM_HTML_MODE_LEGACY));
        btn3.setText(Html.fromHtml(options.get(2), Html.FROM_HTML_MODE_LEGACY));
        btn4.setText(Html.fromHtml(options.get(3), Html.FROM_HTML_MODE_LEGACY));

        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        progressBar.setProgress(100);

        timer = new CountDownTimer(TOTAL_TIME, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (millisUntilFinished * 100 / TOTAL_TIME));
            }

            @Override
            public void onFinish() {
                if (!isAnswered) {
                    updateQuestionAnalytics(false);
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
        boolean isCorrect = selectedText.equals(correctText);

        updateQuestionAnalytics(isCorrect);

        if (isCorrect) {
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

    private void updateQuestionAnalytics(boolean isCorrect) {
        Question currentQuestion = questionList.get(index);
        db.collection("questions").document(currentQuestion.getId())
                .update("timesAnswered", FieldValue.increment(1));

        if (isCorrect) {
            db.collection("questions").document(currentQuestion.getId())
                    .update("timesCorrect", FieldValue.increment(1));
        }
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

    // ... [Inside QuizUIActivity class, keep existing code] ...

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

        // CRITICAL: Pass the difficulty string
        intent.putExtra("DIFFICULTY", currentDifficulty);

        // Optional: Keep this if you need it for other logic
        String quizType = "trivia" + currentDifficulty.substring(0, 1).toUpperCase() + currentDifficulty.substring(1);
        intent.putExtra("QUIZ_TYPE", quizType);
        if (getIntent().getBooleanExtra("IS_GUEST_MODE", false)) {
            intent.putExtra("IS_GUEST_MODE", true);
        }

        startActivity(intent);
        finish();
    }

// ... [End of class] ...
}
