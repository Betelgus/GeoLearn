package com.example.geolearn.game;

import android.graphics.Color;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.geolearn.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class GameAnalysisActivity extends AppCompatActivity {

    private static final String TAG = "GameAnalysisActivity";

    private BarChart barChart;
    private PieChart pieChart;
    private LinearLayout layoutBarContent, layoutPieContent, headerBarChart, headerPieChart;
    private ImageView arrowBar, arrowPie;
    private AutoCompleteTextView dropdownDifficulty;

    private FirebaseFirestore db;
    // private String currentUserId; // No longer needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_analysis);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupToolbar();
        setupClickListeners();
        setupDifficultyFilter();

        fetchChartData("Overall"); // Default filter
    }

    private void initViews() {
        barChart = findViewById(R.id.bar_chart_analysis);
        pieChart = findViewById(R.id.pie_chart_analysis);
        layoutBarContent = findViewById(R.id.layout_bar_content);
        layoutPieContent = findViewById(R.id.layout_pie_content);
        headerBarChart = findViewById(R.id.header_bar_chart);
        headerPieChart = findViewById(R.id.header_pie_chart);
        arrowBar = findViewById(R.id.arrow_bar);
        arrowPie = findViewById(R.id.arrow_pie);
        dropdownDifficulty = findViewById(R.id.dropdown_difficulty);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_analysis);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Global Game Analysis"); // Changed title
        }
    }

    private void setupClickListeners() {
        headerBarChart.setOnClickListener(v -> toggleSection(layoutBarContent, arrowBar));
        headerPieChart.setOnClickListener(v -> toggleSection(layoutPieContent, arrowPie));
    }

    private void setupDifficultyFilter() {
        String[] difficulties = {"Overall", "Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficulties);
        dropdownDifficulty.setAdapter(adapter);
        dropdownDifficulty.setText("Overall", false);

        dropdownDifficulty.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            fetchChartData(selected);
        });
    }

    private void fetchChartData(String difficultyFilter) {
        // --- THIS IS THE MAIN FIX ---
        // The query now fetches from the entire "Scores" collection, not just for one user.
        Query query = db.collection("Scores");

        // Apply difficulty filter if it's not "Overall"
        if (!"Overall".equals(difficultyFilter)) {
            // Capitalize the filter to match what's stored in Firestore ("Beginner", "Intermediate", etc.)
            String capitalizedDifficulty = difficultyFilter.substring(0, 1).toUpperCase() + difficultyFilter.substring(1);
            query = query.whereEqualTo("difficulty", capitalizedDifficulty);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                float totalTriviaScore = 0, totalFlagScore = 0;
                int triviaCount = 0, flagCount = 0;

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    // Check for null values to prevent crashes
                    String gameType = doc.getString("gameType");
                    Long scoreLong = doc.getLong("score");
                    if (scoreLong == null) continue; // Skip if score is missing
                    long score = scoreLong;


                    if (Objects.equals(gameType, "Trivia Quiz")) {
                        totalTriviaScore += score;
                        triviaCount++;
                    } else if (Objects.equals(gameType, "Flag Quiz")) {
                        totalFlagScore += score;
                        flagCount++;
                    }
                }

                float avgTrivia = (triviaCount > 0) ? totalTriviaScore / triviaCount : 0;
                float avgFlag = (flagCount > 0) ? totalFlagScore / flagCount : 0;

                updateBarChart(avgTrivia, avgFlag);
                updatePieChart(triviaCount, flagCount);

            } else {
                Log.e(TAG, "Error fetching scores: ", task.getException());
                Toast.makeText(this, "Failed to load global analytics.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // No changes needed in updateBarChart, updatePieChart, toggleSection, or onOptionsItemSelected
    // ... (rest of the file is the same)
    private void updateBarChart(float avgTrivia, float avgFlag) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, avgTrivia));
        entries.add(new BarEntry(1, avgFlag));

        BarDataSet dataSet = new BarDataSet(entries, "Average Scores");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1200);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Trivia Quiz", "Flag Quiz"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f); // Assuming max score is 10
        leftAxis.setLabelCount(6, true);

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private void updatePieChart(int triviaCount, int flagCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if(triviaCount > 0) entries.add(new PieEntry(triviaCount, "Trivia Quiz"));
        if(flagCount > 0) entries.add(new PieEntry(flagCount, "Flag Quiz"));

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText("No Data");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Game Popularity");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.animateY(1200);
        pieChart.invalidate();
    }

    private void toggleSection(View content, ImageView arrow) {
        TransitionManager.beginDelayedTransition((ViewGroup) content.getParent());
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            arrow.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            content.setVisibility(View.VISIBLE);
            arrow.setImageResource(android.R.drawable.arrow_up_float);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
