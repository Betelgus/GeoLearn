package com.example.geolearn.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geolearn.R;

public class FeedbackHistory extends AppCompatActivity {

    ImageView btnEdit, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_history);

        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Edit button -> go to edit page
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(FeedbackHistory.this,
                    FeedbackEditActivity.class);
            startActivity(intent);
        });
    }
}
