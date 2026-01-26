package com.padgettanna.weighttracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.padgettanna.weighttracker.model.WeightEntry;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Main screen for the Weight Tracker app.
 * - Displays greeting, current and goal weight
 * - Allows user to add new weight, or update their goal weight
 * - Allows to navigate to the weight log activity
 * - Sends SMS notifications if the user reaches their goal weight after requesting permission
 */
public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private Button saveButton;
    private Button saveGoalButton;
    private EditText newWeightEditText;
    private EditText newGoalWeightEditText;
    private TextView greetingText;
    private TextView goalWeightValueText;
    private TextView unitsText;
    private TextView currentWeightValueText;
    private TextView rollingAverageText;
    private TextView trendText;

    // Database helper
    private WTDatabaseHelper wtDB;
    // User email passed from login activity
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Get user email from login activity
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        Log.d("Main Activity", "Received email: " + userEmail);
        
        // Initialize UI elements
        Button addButton = findViewById(R.id.buttonAdd);
        Button viewLogButton = findViewById(R.id.buttonViewLog);
        saveButton = findViewById(R.id.buttonSave);
        Button newGoalWeightButton = findViewById(R.id.buttonUpdateGoalWeight);
        saveGoalButton = findViewById(R.id.buttonSaveGoal);
        newWeightEditText = findViewById(R.id.editTextNewWeight);
        newGoalWeightEditText = findViewById(R.id.editTextNewGoalWeight);
        greetingText = findViewById(R.id.textGreeting);
        goalWeightValueText = findViewById(R.id.textGoalValue);
        unitsText = findViewById(R.id.textUnits2);
        currentWeightValueText = findViewById(R.id.textCurrentValue);
        rollingAverageText = findViewById(R.id.textRollingAverage);
        trendText = findViewById(R.id.textTrend);

        // Initialize database helper
        wtDB = new WTDatabaseHelper(MainActivity.this);

        // If user email was not passed via intent, retrieve from shared preferences
        if (userEmail == null) {
            userEmail = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    .getString("USER_EMAIL", null);
            Log.d("Main Activity", "Updated email: " + userEmail);
        }

        // Load user's profile and weight data from the database
        loadUserData();
        updateInsights();

        // Disable save buttons
        saveButton.setEnabled(false);
        saveGoalButton.setEnabled(false);

        // ----- Button Listeners -----

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show input field and save button to add new weight
                newWeightEditText.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                // Hide input field and save button to set new goal weight
                newGoalWeightEditText.setVisibility(View.GONE);
                saveGoalButton.setVisibility(View.GONE);
            }
        });

        newWeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            // Enable save button when user enters new weight
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(!s.toString().isEmpty());
            }
        });

        // Save new weight entry to the database
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newWeight = newWeightEditText.getText().toString();
                String goalWeight = goalWeightValueText.getText().toString();
                if (goalWeight.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please set a goal weight first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newWeight.isEmpty() && !goalWeight.isEmpty()) {
                    int currentWt = Integer.parseInt(newWeight);
                    int goalWt = Integer.parseInt(goalWeight);
                    LocalDate todayDate = LocalDate.now();

                    // Add new weight to database with today's date
                    boolean success = wtDB.addWeight(todayDate.toString(), currentWt, userEmail);

                    if (!success) { Toast.makeText(MainActivity.this, "Weight must be between 50 and 999.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Update current weight field, average, and trend
                    refreshCurrentWeight();
                    updateInsights();

                    // Hide input field and save button
                    newWeightEditText.setText("");
                    newWeightEditText.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);

                    // Send message if goal weight achieved
                    if (currentWt <= goalWt) {
                        sendSMSMessage();
                    }
                }
            }
        });

        newGoalWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show input field and save button to set new goal weight
                newGoalWeightEditText.setVisibility(View.VISIBLE);
                saveGoalButton.setVisibility(View.VISIBLE);
                // Hide input field and save button to add new weight
                newWeightEditText.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
            }
        });

        newGoalWeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable save button when weight is entered
                saveGoalButton.setEnabled(!s.toString().isEmpty());
            }
        });

        // Save new goal weight to the database
        saveGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newGoalWeight = newGoalWeightEditText.getText().toString().trim();

                if (!newGoalWeight.isEmpty()) {
                    int goalWt;
                    try {
                        goalWt = Integer.parseInt(newGoalWeight);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Goal weight must be a number.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = wtDB.setGoalWeight(goalWt, userEmail);

                    if (!success) {
                        Toast.makeText(MainActivity.this, "Goal weight must be between 50 and 999.", Toast.LENGTH_LONG).show();
                        return; // stay on screen
                    }

                    // Only update UI if DB write succeeded
                    goalWeightValueText.setText(String.valueOf(goalWt));

                    // Hide input field and save button
                    newGoalWeightEditText.setText("");
                    newGoalWeightEditText.setVisibility(View.GONE);
                    saveGoalButton.setVisibility(View.GONE);
                }
            }
        });

        // Navigate to the weight log activity
        viewLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch WTLog activity
                Intent intent = new Intent(MainActivity.this, WTLogActivity.class);
                intent.putExtra("USER_EMAIL", userEmail); // pass user email to WTLogActivity
                startActivity(intent);
            }
        });
    }

    // Loads user data from the database, updates TextViews on the screen
    private void loadUserData() {
        // Load user's name
        Cursor cursorName  = wtDB.readUserName(userEmail);
        if(cursorName.moveToNext()) {
            String userName = cursorName.getString(0);
            greetingText.setText("Hello, " + userName + "!");
        }
        cursorName.close();

        // Load user's goal weight
        Cursor cursorGoalWeight  = wtDB.readGoalWeight(userEmail);
        if(cursorGoalWeight.moveToNext()) {
            String goalFromDB = cursorGoalWeight.getString(0);
            goalWeightValueText.setText(goalFromDB);
        }
        cursorGoalWeight.close();

        // Load user's current weight if available
        Cursor cursorCurrentWeight  = wtDB.readCurrentWeight(userEmail);
        if (cursorCurrentWeight != null && cursorCurrentWeight.getCount() > 0) {
            cursorCurrentWeight.moveToNext();
            String weightFromDB = cursorCurrentWeight.getString(0);
            currentWeightValueText.setText(weightFromDB);
            unitsText.setVisibility(View.VISIBLE);
        }
        else {
            Log.i("Main Activity", "No weight entries found for the user");
        }
        if (cursorCurrentWeight != null) {
            cursorCurrentWeight.close();
        }
    }

    // Refresh current weight when returning from another activity
    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentWeight();
        updateInsights();
    }

    // Retrieves latest weight entry from the database
    private void refreshCurrentWeight() {
        Cursor cursorCurrentWeight = wtDB.readCurrentWeight(userEmail);

        if (cursorCurrentWeight != null && cursorCurrentWeight.getCount() > 0) {
            cursorCurrentWeight.moveToNext();
            currentWeightValueText.setText(cursorCurrentWeight.getString(0));
            unitsText.setVisibility(View.VISIBLE);
        }
        else {
            currentWeightValueText.setText("");
            unitsText.setVisibility(View.GONE);
        }
        if (cursorCurrentWeight != null) {
            cursorCurrentWeight.close();
        }
    }

    private void updateInsights() {
        List<WeightEntry> entries = wtDB.getWeightEntries(userEmail);
        if (entries.isEmpty()) {
            rollingAverageText.setText("--");
            trendText.setText("No data");
            return;
        }

        if (entries.size() == 1) {
            rollingAverageText.setText(String.valueOf(entries.get(0).getWeight()));
            trendText.setText("Not enough data");
            return;
        }

        // Ensure chronological order
        Collections.sort(entries,
                (a, b) -> a.getDate().compareTo(b.getDate()));

        // Rolling averages
        List<Double> averages =
                WeightAnalysisUtil.rollingAverage(entries, 7);

        double latestAvg = averages.get(averages.size() - 1);
        rollingAverageText.setText(String.format("%.1f", latestAvg));

        // Trend detection
        WeightAnalysisUtil.Trend trend =
                WeightAnalysisUtil.detectTrend(averages, 0.5);

        switch (trend) {
            case DOWNWARD:
                trendText.setText("Downward ↓");
                break;
            case UPWARD:
                trendText.setText("Upward ↑");
                break;
            default:
                trendText.setText("Stable →");
        }
    }

    // Send SMS when goal weight is reached
    private void sendSMSMessage() {
        // SMS info
        String phoneNumber = "11111111111";
        String message = "Congratulations! You have reached your goal weight!";
        try {
            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                // Send SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                // Notify user
                Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
                Log.i("Weight Tracker SMS", "SMS sent to " + phoneNumber + " with message: " + message);
            }
        } catch (Exception e) {
            // Handle errors
            Toast.makeText(getApplicationContext(), "SMS failed to send.", Toast.LENGTH_LONG).show();
            Log.e("Weight Tracker SMS", "SMS failed to send to " + phoneNumber, e);
        }
    }
}