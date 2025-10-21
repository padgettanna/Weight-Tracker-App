package com.padgettanna.weighttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Update/Delete screen for the Weight Tracker app.
 * - Prompts user to update or delete selected entry
 * - Updates or removes existing entry from the database
 */
public class UpdateDeleteActivity extends AppCompatActivity {
    // UI elements
    EditText dateEditText, weightEditText;
    Button updateButton, deleteButton;
    // Database helper
    private WTDatabaseHelper wtDB;
    // Variables to store data from the log activity
    int id, weight;
    String date, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_entry);

        // Initialize UI elements
        dateEditText = findViewById(R.id.editTextDate);
        weightEditText = findViewById(R.id.editTextWeight);
        updateButton = findViewById(R.id.buttonUpdate);
        deleteButton = findViewById(R.id.buttonDelete);
        // Initialize database helper
        wtDB = new WTDatabaseHelper(UpdateDeleteActivity.this);

        getAndSetIntentData();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                // Get date and weight values from user input
                String newDate = dateEditText.getText().toString().trim();
                String newWeight = weightEditText.getText().toString().trim();

                // Check if user entered new data
                if(newDate.isEmpty() || newWeight.isEmpty()) {
                    Toast.makeText(UpdateDeleteActivity.this, "Fields cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                int newWt = Integer.parseInt(newWeight);

                // Update database with new data
                wtDB.updateWeightEntry(id, newDate, newWt, userEmail);

                // Return to WTLog activity
                setResult(RESULT_OK);
                finish();
            }
        });

        // Display Alert Dialog when delete button is pressed
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                confirmDeleteDialog();
            }
        });
    }

    // Retrieve intent data from CustomAdapter, save it into variables,
    // populate EditText fields with values from intent
    void getAndSetIntentData() {
        if(getIntent().hasExtra("USER_EMAIL")) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }
        if (userEmail == null) {
            userEmail = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    .getString("USER_EMAIL", null);
        }

        if(getIntent().hasExtra("id") && getIntent().hasExtra("date") &&
                getIntent().hasExtra("weight")) {
            // Get data from intent
            id = getIntent().getIntExtra("id", -1);
            date = getIntent().getStringExtra("date");
            weight = getIntent().getIntExtra("weight", -1);

            // Set intent data
            dateEditText.setText(date);
            weightEditText.setText(String.valueOf(weight));
        }
        else {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }
    }

    // Alert Dialog asks user to confirm entry deletion
    void confirmDeleteDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this entry?");
        builder.setMessage("Are you sure you want to delete " + date + " " + weight + " entry?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                wtDB.deleteWeightEntry(id, userEmail);
                // Return to WTLog activity
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}
