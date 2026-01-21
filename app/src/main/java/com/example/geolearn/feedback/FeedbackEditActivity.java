package com.example.geolearn.feedback;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackEditActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etFeedback;
    private Button btnUpdate, btnDelete;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_edit);

        db = FirebaseFirestore.getInstance();

        ratingBar = findViewById(R.id.ratingBarEdit);
        etFeedback = findViewById(R.id.etFeedbackEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        documentId = getIntent().getStringExtra("documentId");
        String text = getIntent().getStringExtra("feedbackText");
        float rating = getIntent().getFloatExtra("rating", 0);

        if (text != null) etFeedback.setText(text);
        ratingBar.setRating(rating);

        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateFeedback());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void updateFeedback() {
        if (documentId == null) {
            Toast.makeText(this, "Error: Document ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        String feedback = etFeedback.getText().toString().trim();

        if (feedback.isEmpty() || rating == 0) {
            Toast.makeText(this, "Please provide both rating and feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("feedbackText", feedback);
        updates.put("rating", rating);
        updates.put("timestamp", System.currentTimeMillis());

        db.collection("feedback").document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Feedback updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Feedback")
                .setMessage("Are you sure you want to delete this feedback?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFeedback())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFeedback() {
        if (documentId == null) return;

        db.collection("feedback").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Feedback deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
