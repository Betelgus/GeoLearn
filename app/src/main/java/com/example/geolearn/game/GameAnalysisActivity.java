package com.example.geolearn.game;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.geolearn.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class GameAnalysisActivity extends AppCompatActivity {

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar_analysis);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        barChart = findViewById(R.id.bar_chart_analysis);
        setupChart();
    }

    private void setupChart() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1f, 85f));
        barEntries.add(new BarEntry(2f, 45f));
        barEntries.add(new BarEntry(3f, 60f));
        barEntries.add(new BarEntry(4f, 90f));
        barEntries.add(new BarEntry(5f, 75f));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Game Performance");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setDrawValues(true);

        barChart.setData(new BarData(barDataSet));
        barChart.animateXY(2000, 2000);
        barChart.getDescription().setText("Recent Scores");
        barChart.getDescription().setTextColor(Color.BLUE);
        barChart.invalidate();
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
