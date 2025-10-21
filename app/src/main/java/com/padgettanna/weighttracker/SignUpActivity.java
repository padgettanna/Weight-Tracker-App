package com.padgettanna.weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStructure;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Sign Up screen for the Weight Tracker app.
 * - Prompts user to enter name, goal weight, email, and password
 * - Saves user's information into the database
 * - After successful account creation directs user to the main screen
 */
public class SignUpActivity extends AppCompatActivity {

    // UI elements
    private Button signupButton;
    private EditText nameEditText;
    private EditText goalWeightEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    // Database helper
    private WTDatabaseHelper wtDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI elements
        signupButton = findViewById(R.id.buttonSignUp2);
        nameEditText = findViewById(R.id.editTextName);
        goalWeightEditText = findViewById(R.id.editTextGoalWeight);
        emailEditText = findViewById(R.id.editTextEmail2);
        passwordEditText = findViewById(R.id.editTextPassword2);
        // Initialize database helper
        wtDB = new WTDatabaseHelper(SignUpActivity.this);

        // Disable sign up button
        signupButton.setEnabled(false);

        // TextWatcher to monitor EditText fields
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            // Enable signup button only if name, goalWeight, email and password fields are not empty
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = nameEditText.getText().toString();
                String goalWeight = goalWeightEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signupButton.setEnabled(!name.isEmpty() && !goalWeight.isEmpty() && !email.isEmpty()
                && !password.isEmpty());
            }
        };

        // Attach the text watcher to EditText fields
        nameEditText.addTextChangedListener(textWatcher);
        goalWeightEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        // Save user input into the database
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String name = nameEditText.getText().toString();
                String goalWeight = goalWeightEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                int goalWt = Integer.parseInt(goalWeight);
                // Add name, email, password, goal weight to database
                wtDB.addUser(name, email, password);
                wtDB.setGoalWeight(goalWt, email);

                // Save user email in SharedPreferences
                getSharedPreferences("UserPreferences", MODE_PRIVATE).edit()
                        .putString("USER_EMAIL", email).apply();
                // Launch MainActivity when login button is pressed
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.putExtra("USER_EMAIL", email); // pass user email to MainActivity
                startActivity(intent);
            }
        });
    }
}