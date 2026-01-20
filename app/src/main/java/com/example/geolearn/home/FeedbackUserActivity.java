package com.example.geolearn.home;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;

public class FeedbackUserActivity extends AppCompatActivity {

    EditText etName, etFeedback;
    RatingBar ratingBar;
    Button btnSubmit;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_user); // ✅ MATCH XML NAME

        etName = findViewById(R.id.etName);
        etFeedback = findViewById(R.id.etFeedback);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Submit feedback
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String name = etName.getText().toString().trim();
        String feedback = etFeedback.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (name.isEmpty() || feedback.isEmpty() || rating == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save to database / Firebase later
        Toast.makeText(this,
                "Thank you for your feedback, " + name + "!",
                Toast.LENGTH_LONG).show();

        // Clear fields
        etName.setText("");
        etFeedback.setText("");
        ratingBar.setRating(0);
    }
}
