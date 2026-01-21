package com.example.geolearn.feedback;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackUserActivity extends AppCompatActivity {

    private EditText etFeedback;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private ImageView btnBack;

    // Firebase instances
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_user);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etFeedback = findViewById(R.id.etFeedback);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String feedbackText = etFeedback.getText().toString().trim();
        float rating = ratingBar.getRating();

        // Get Current User ID
        String userId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "guest";

        if (feedbackText.isEmpty() || rating == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Prepare Data Map
        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("userId", userId);
        feedbackMap.put("feedbackText", feedbackText);
        feedbackMap.put("rating", rating);
        feedbackMap.put("timestamp", System.currentTimeMillis());

        // Disable button to prevent multiple submissions
        btnSubmit.setEnabled(false);

        // 2. Save to Firestore "feedback" collection
        firestore.collection("feedback")
                .add(feedbackMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thank you for the Feedback!", Toast.LENGTH_LONG).show();

                    // Clear fields
                    etFeedback.setText("");
                    ratingBar.setRating(0);

                    // Go back to previous screen
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}