package com.example.geolearn.feedback;
import com.example.geolearn.R;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        btnBack = findViewById(R.id.btnBack);

        // Back button action
        btnBack.setOnClickListener(v -> finish());
    }
}
