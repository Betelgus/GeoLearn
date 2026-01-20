package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;

public class FeedbackHistory extends AppCompatActivity {

    TextView btnEdit; // changed from ImageView
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_history);

        // Initialize views
        btnEdit = findViewById(R.id.btnEdit);  // TextView now
        btnBack = findViewById(R.id.btnBack);  // ImageView

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Edit button -> go to edit page
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(FeedbackHistory.this,
                    FeedbackUserActivity.class); // updated target activity
            startActivity(intent);
        });
    }
}
