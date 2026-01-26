package com.padgettanna.weighttracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;

/**
 * Update/Delete screen for the Weight Tracker app.
 * Responsibilities:
 * - Displays an existing weight entry
 * - Allows the user to update the entry with validated input
 * - Allows deletion with confirmation
 * This activity relies on WTDatabaseHelper for validation
 * and reacts to boolean success/failure results to control UI flow.
 */
public class UpdateDeleteActivity extends AppCompatActivity {
    // UI elements
    EditText dateEditText, weightEditText;
    Button updateButton, deleteButton;
    // Database helper
    private WTDatabaseHelper wtDB;
    // Variables to store data from the log activity
    int id;
    String userEmail;

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

        dateEditText.setOnClickListener(v -> showDatePicker());
        updateButton.setOnClickListener(view -> handleUpdate());
        deleteButton.setOnClickListener(view -> confirmDeleteDialog());

    }

    /**
     * Handles update logic:
     * - Performs basic UI validation
     * - Delegates business validation to the database helper
     * - Keeps the user on the screen if validation fails
     */
    private void handleUpdate() {
        String newDate = dateEditText.getText().toString().trim();
        String newWeightText = weightEditText.getText().toString().trim();

        if (newDate.isEmpty() || newWeightText.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int newWeight;
        try {
            newWeight = Integer.parseInt(newWeightText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Weight must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = wtDB.updateWeightEntry(id, newDate, newWeight, userEmail);

        if (!success) {
            Toast.makeText(
                    this,
                    "Invalid input. Please check date and weight values.",
                    Toast.LENGTH_LONG).show();
            // Stay on this screen
            return;
        }

        // Success
        setResult(RESULT_OK);
        finish();
    }


    /**
     * Retrieves entry data passed from the log activity.
     * Terminates the activity if required data is missing.
     */
    void getAndSetIntentData() {
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userEmail == null) {
            userEmail = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    .getString("USER_EMAIL", null);
        }

        if (!getIntent().hasExtra("id")
                || !getIntent().hasExtra("date")
                || !getIntent().hasExtra("weight")) {

            Toast.makeText(this, "No entry data received.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        id = getIntent().getIntExtra("id", -1);
        dateEditText.setText(getIntent().getStringExtra("date"));
        weightEditText.setText(String.valueOf(getIntent().getIntExtra("weight", 0)));
    }

    /**
     * Confirms deletion using the current field values
     * to avoid stale state issues.
     */
    void confirmDeleteDialog () {
        String currentDate = dateEditText.getText().toString();
        String currentWeight = weightEditText.getText().toString();

        new AlertDialog.Builder(this)
            .setTitle("Delete this entry?")
            .setMessage("Are you sure you want to delete " + currentDate + " (" + currentWeight + ")?")
            .setPositiveButton("Yes", (dialog, which) -> {
                boolean success = wtDB.deleteWeightEntry(id, userEmail);

            if (!success) {
                Toast.makeText(this, "Failed to delete entry.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Return to WTLog activity
            setResult(RESULT_OK);
            finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    /**
     * Displays a DatePickerDialog limited to today or earlier.
     */
    private void showDatePicker() {
        LocalDate today = LocalDate.now();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selected = LocalDate.of(year, month + 1, dayOfMonth);
                    dateEditText.setText(selected.toString());
                },
                today.getYear(),
                today.getMonthValue() - 1,
                today.getDayOfMonth()
        );

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }
}
