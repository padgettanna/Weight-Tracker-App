package com.padgettanna.weighttracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Login screen for the Weight Tracker app.
 * - Displays greeting, prompts user to log in
 * - Verifies user's account is in database, and the password matches
 * - Gives user the option to create new account
 * - Allows user to use app in the guest mode, bypassing account creation
 */
public class LoginActivity extends AppCompatActivity {

    // UI elements
    private Button loginButton;
    private Button signupButton;
    private Button guestAccessButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    // Database helper
    private WTDatabaseHelper wtDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        loginButton = findViewById(R.id.buttonLogin);
        signupButton = findViewById(R.id.buttonSignUp);
        guestAccessButton = findViewById(R.id.buttonGuestSignIn);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        // Initialize database helper
        wtDB = new WTDatabaseHelper(LoginActivity.this);

        // Disable login button
        loginButton.setEnabled(false);

        // TextWatcher to monitor editText fields
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            // Enable login button only if email and password fields are not empty
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                loginButton.setEnabled(!email.isEmpty() && !password.isEmpty());
            }
        };

        // Attach the text watcher to EditText fields
        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Search for user email in the database
                Cursor cursor  = wtDB.authenticateUser(email);
                String databasePassword;

                // Check if account exists
                if (cursor.getCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Account not found", Toast.LENGTH_SHORT).show();
                }
                else {
                    cursor.moveToNext();
                    databasePassword = cursor.getString(0);

                    // Compare entered password matches the one in the database
                    if (password.equals(databasePassword)) {
                        // Save user email in SharedPreferences
                        getSharedPreferences("UserPreferences", MODE_PRIVATE).edit()
                                .putString("USER_EMAIL", email).apply();
                        // Launch MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_EMAIL", email); // pass user email to MainActivity
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Incorrect password. Try again", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch SignUpActivity when signup button is pressed
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Allows user to access main screen without logging in or creating an account
        guestAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guestEmail = "guest@guest.com";
                // Check if guest account exists in the database
                Cursor cursor = wtDB.authenticateUser(guestEmail);
                // If guest account does not exist in the database, create one with default values
                if (cursor == null || cursor.getCount() == 0) {
                    wtDB.addUser("Guest", guestEmail, "");
                    wtDB.setGoalWeight(100, guestEmail);
                }
                // Launch MainActivity when continue as guest button is pressed
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // Save user email in SharedPreferences
                getSharedPreferences("UserPreferences", MODE_PRIVATE).edit()
                        .putString("USER_EMAIL", guestEmail).apply();
                intent.putExtra("USER_EMAIL", guestEmail); // pass user email to MainActivity
                startActivity(intent);
            }
        });
    }
}