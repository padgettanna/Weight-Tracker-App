package com.padgettanna.weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.padgettanna.weighttracker.model.WeightEntry;

import java.util.Collections;
import java.util.List;

/**
 * Weight log for the Weight Tracker app.
 * - Displays weight entries using structured WeightEntry objects.
 */
public class WTLogActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private WTDatabaseHelper wtDB;
    CustomAdapter customAdapter;
    // User email from main activity
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wtlog);

        // Initialize variables
        recyclerView = findViewById(R.id.recyclerViewLog);
        wtDB = new WTDatabaseHelper(WTLogActivity.this);

        // Get user email from main activity
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userEmail == null) {
            userEmail = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    .getString("USER_EMAIL", null);
        }

        // Read data from database into list
        loadWeightEntries();
    }

    // Reset activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            loadWeightEntries(); // refresh list after update/delete
        }
    }

    void loadWeightEntries() {
        List<WeightEntry> entries = wtDB.getWeightEntries(userEmail);

        if (entries.isEmpty()) {
            Toast.makeText(this, "No weight entries found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ANALYSIS (chronological order: oldest -> newest)
        List<WeightEntry> chronological = new java.util.ArrayList<>(entries);
        java.util.Collections.sort(chronological); // assumes WeightEntry sorts by date asc

        List<Double> avgs = WeightAnalysisUtil.rollingAverage(chronological, 7); // 7-entry window
        WeightAnalysisUtil.Trend trend = WeightAnalysisUtil.detectTrend(avgs, 0.5); // threshold

        Log.d("WeightAnalysis", "Rolling avg (last): " +
                (avgs.isEmpty() ? "n/a" : avgs.get(avgs.size() - 1)));
        Log.d("WeightAnalysis", "Trend: " + trend);

        // Sort by date (most recent first)
        Collections.sort(entries, Collections.reverseOrder());

        customAdapter = new CustomAdapter(this, this, entries, userEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(customAdapter);
    }
}