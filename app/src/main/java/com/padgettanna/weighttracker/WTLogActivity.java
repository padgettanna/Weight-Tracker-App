package com.padgettanna.weighttracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Weight log for the Weight Tracker app.
 * - Retrieves and displays each weight entry from the database using CustomAdapter class
 */
public class WTLogActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private WTDatabaseHelper wtDB;
    private ArrayList<Integer> entryId, entryValue;
    private ArrayList<String> entryDate, entryEmail;
    CustomAdapter customAdapter;
    // User email from main activity
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wtlog);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize variables
        recyclerView = findViewById(R.id.recyclerViewLog);
        wtDB = new WTDatabaseHelper(WTLogActivity.this);
        entryId = new ArrayList<>();
        entryDate = new ArrayList<>();
        entryValue = new ArrayList<>();
        entryEmail = new ArrayList<>();
        // Get user email from main activity
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        Log.d("WTLog Activity", "Received email: " + userEmail);

        if (userEmail == null) {
            userEmail = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    .getString("USER_EMAIL", null);
            Log.d("WTLog Activity", "Updated email: " + userEmail);
        }

        // Read data from database into arrays
        storeDataInArrays();
        // Initialize adapter with data arrays
        customAdapter = new CustomAdapter(WTLogActivity.this, this, entryId, entryDate, entryValue, entryEmail);
        // Set adapter to recycler view
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(WTLogActivity.this));
    }

    // Reset activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            entryId.clear();
            entryDate.clear();
            entryValue.clear();
            entryEmail.clear();

            storeDataInArrays();
            customAdapter.notifyDataSetChanged();
        }
    }

    // Helper method to read data from database into arrays
    void storeDataInArrays() {
        Cursor cursor = wtDB.readAllData(userEmail);
        // Check if database is empty
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }
        else {
            // Loop through each row in cursor
            while (cursor.moveToNext()) {
                entryId.add(cursor.getInt(0));
                entryDate.add(cursor.getString(1));
                entryValue.add(cursor.getInt(2));
                entryEmail.add(cursor.getString(3));
            }
            cursor.close();
        }
    }
}