package com.example.geolearn.game;

import android.graphics.Color;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.geolearn.R;
// --- Imports for Charts ---
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
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

import java.util.ArrayList;

public class GameAnalysisActivity extends AppCompatActivity {

    private BarChart barChart;
    private LinearLayout layoutBarContent;
    private LinearLayout headerBarChart;
    private ImageView arrowBar;

    private PieChart pieChart;
    private LinearLayout layoutPieContent;
    private LinearLayout headerPieChart;
    private ImageView arrowPie;

    private ViewGroup mainContentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_analysis);

        setupToolbar();
        initViews();

        // Setup the specific analytics from your suggestions
        setupScoreDistributionChart(); // Idea #1: Score Distribution
        setupCategoryPopularityChart(); // Idea #4: Category Popularity

        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_analysis);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        mainContentLayout = findViewById(R.id.main_content_layout);

        barChart = findViewById(R.id.bar_chart_analysis);
        layoutBarContent = findViewById(R.id.layout_bar_content);
        headerBarChart = findViewById(R.id.header_bar_chart);
        arrowBar = findViewById(R.id.arrow_bar);

        pieChart = findViewById(R.id.pie_chart_analysis);
        layoutPieContent = findViewById(R.id.layout_pie_content);
        headerPieChart = findViewById(R.id.header_pie_chart);
        arrowPie = findViewById(R.id.arrow_pie);
    }

    private void setupClickListeners() {
        headerBarChart.setOnClickListener(v -> toggleSection(layoutBarContent, arrowBar));
        headerPieChart.setOnClickListener(v -> toggleSection(layoutPieContent, arrowPie));
    }

    private void toggleSection(View content, ImageView arrow) {
        TransitionManager.beginDelayedTransition(mainContentLayout, new AutoTransition());
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            arrow.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            content.setVisibility(View.VISIBLE);
            arrow.setImageResource(android.R.drawable.arrow_up_float);
        }
    }

    // --- ANALYTICS IDEA 1: Average Score & Distribution ---
    private void setupScoreDistributionChart() {
        ArrayList<BarEntry> scores = new ArrayList<>();
        scores.add(new BarEntry(0, 65f)); // Game 1
        scores.add(new BarEntry(1, 80f)); // Game 2
        scores.add(new BarEntry(2, 45f)); // Game 3
        scores.add(new BarEntry(3, 90f)); // Game 4
        scores.add(new BarEntry(4, 85f)); // Game 5

        BarDataSet set = new BarDataSet(scores, "Score History");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(12f);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f); // Make bars a bit thinner

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(1000);

        // Customize X Axis to show "Game X"
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        String[] games = new String[]{"Game 1", "Game 2", "Game 3", "Game 4", "Game 5"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(games));

        // Add an "Average Score" Limit Line
        LimitLine avgLine = new LimitLine(73f, "Avg Score: 73");
        avgLine.setLineWidth(2f);
        avgLine.setLineColor(Color.RED);
        avgLine.setTextColor(Color.BLACK);
        avgLine.setTextSize(10f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.addLimitLine(avgLine);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        barChart.getAxisRight().setEnabled(false); // Hide right axis
    }

    // --- ANALYTICS IDEA 4: Quiz Category Popularity ---
    private void setupCategoryPopularityChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        // Showing which categories the user plays most
        entries.add(new PieEntry(40f, "Flags"));
        entries.add(new PieEntry(30f, "Maps"));
        entries.add(new PieEntry(20f, "Capitals"));
        entries.add(new PieEntry(10f, "Population"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.JOYFUL_COLORS); // Colorful look for categories
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(14f);

        PieData data = new PieData(set);
        pieChart.setData(data);

        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Favorite\nCategory");
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(40f); // Donut hole
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);
        pieChart.invalidate();
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