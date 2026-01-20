package com.example.geolearn.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;

public class FeedbackEditActivity extends AppCompatActivity {

    RatingBar ratingBar;
    EditText etFeedback;
    Button btnUpdate, btnDelete;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_edit);

        ratingBar = findViewById(R.id.ratingBarEdit);
        etFeedback = findViewById(R.id.etFeedbackEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // Example: preload existing feedback
        ratingBar.setRating(4);
        etFeedback.setText("Great app! I enjoy learning geography.");

        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String feedback = etFeedback.getText().toString().trim();

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Update feedback in database
            Toast.makeText(this, "Feedback updated successfully!", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            // TODO: Delete feedback from database
            Toast.makeText(this, "Feedback deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
