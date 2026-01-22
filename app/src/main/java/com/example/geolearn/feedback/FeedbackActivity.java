package com.example.geolearn.feedback;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geolearn.R;
import com.example.geolearn.home.HomeFeedbackAdapter; // Reusing your existing adapter
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvFeedbackHistory;
    private HomeFeedbackAdapter adapter;
    private List<feedback> feedbackList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup UI
        btnBack = findViewById(R.id.btnBack);
        rvFeedbackHistory = findViewById(R.id.rvFeedbackHistory);

        // Setup RecyclerView
        rvFeedbackHistory.setLayoutManager(new LinearLayoutManager(this));
        feedbackList = new ArrayList<>();
        adapter = new HomeFeedbackAdapter(feedbackList);
        rvFeedbackHistory.setAdapter(adapter);

        // Load all data
        loadAllFeedback();

        // Back button action
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAllFeedback() {
        // Fetch ALL feedbacks ordered by newest first
        db.collection("feedback")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    feedbackList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        feedback f = doc.toObject(feedback.class);
                        if (f != null) {
                            fetchUsernameForFeedback(f);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FeedbackActivity", "Error loading data", e));
    }

    private void fetchUsernameForFeedback(feedback f) {
        db.collection("users").document(f.userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        f.username = userDoc.getString("username");
                    } else {
                        f.username = "Anonymous";
                    }
                    feedbackList.add(f);
                    adapter.notifyDataSetChanged();
                });
    }
}