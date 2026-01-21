package com.example.geolearn.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geolearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedbackHistory extends AppCompatActivity {

    private RecyclerView rvFeedback;
    private FeedbackAdapter adapter;
    private List<feedback> feedbackList;
    private FirebaseFirestore db;
    private TextView tvEmpty;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_history);

        db = FirebaseFirestore.getInstance();
        rvFeedback = findViewById(R.id.rvFeedback);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        TextView btnAddFeedback = findViewById(R.id.btnAddFeedback);
        if (btnAddFeedback != null) {
            btnAddFeedback.setOnClickListener(v -> {
                Intent intent = new Intent(FeedbackHistory.this, FeedbackUserActivity.class);
                startActivity(intent);
            });
        }

        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        feedbackList = new ArrayList<>();
        rvFeedback.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FeedbackAdapter(feedbackList, f -> {
            // Navigate to Edit Activity
            Intent intent = new Intent(FeedbackHistory.this, FeedbackEditActivity.class);
            intent.putExtra("documentId", f.documentId);
            intent.putExtra("feedbackText", f.feedbackText);
            intent.putExtra("rating", f.rating);
            startActivity(intent);
        });
        
        rvFeedback.setAdapter(adapter);

        fetchFeedback();
    }

    private void fetchFeedback() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            tvEmpty.setText("Please log in to view feedback history.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("feedback")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    feedbackList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        feedback f = doc.toObject(feedback.class);
                        if (f != null) {
                            f.documentId = doc.getId(); // Capture the document ID for editing
                            feedbackList.add(f);
                        }
                    }

                    if (feedbackList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from FeedbackEditActivity
        fetchFeedback();
    }
}
