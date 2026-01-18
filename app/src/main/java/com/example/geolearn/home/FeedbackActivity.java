package com.example.geolearn.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.geolearn.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class FeedbackActivity extends AppCompatActivity {

    private ListView listViewFeedback;
    private FloatingActionButton fabAddFeedback;

    // Static list keeps data while app is running
    private static ArrayList<String> feedbackList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private boolean isGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        isGuest = getIntent().getBooleanExtra("IS_GUEST", false);

        listViewFeedback = findViewById(R.id.listViewFeedback);
        fabAddFeedback = findViewById(R.id.fabAddFeedback);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Dummy Data
        if (feedbackList.isEmpty()) {
            feedbackList.add("User1: Great app!");
            feedbackList.add("User2: I learned so much about flags.");
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, feedbackList);
        listViewFeedback.setAdapter(adapter);

        // HIDE button if Guest
        if (isGuest) {
            fabAddFeedback.setVisibility(View.GONE);
        } else {
            fabAddFeedback.setVisibility(View.VISIBLE);
            fabAddFeedback.setOnClickListener(v -> showAddDialog());
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_feedback);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etFeedback = dialog.findViewById(R.id.etFeedback);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String text = etFeedback.getText().toString().trim();
            if (!text.isEmpty()) {
                feedbackList.add(0, "You: " + text);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Feedback Added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}