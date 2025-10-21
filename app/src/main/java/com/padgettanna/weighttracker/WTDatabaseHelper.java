package com.padgettanna.weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Database helper class manages the SQLite database for the Weight Tracker app.
 * - Contains three tables: weight_log (stores individual weight entries)
 *                        user_table (stores user personal data)
 *                        goal_table (stores user's goal weight)
 * - Contains methods to create, read, update, and delete data from the database
 */
public class WTDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    // Database configuration
    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 7;

    // Weight log table
    private static final String TABLE_LOG = "weight_log";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";

    // User info table
    private static final String TABLE_USER = "user_table";
    private static final String COLUMN_USER_ID = "_id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_NAME = "name";

    // Goal weight table
    private static final String TABLE_GOAL = "goal_table";
    private static final String COLUMN_GOAL_ID = "_id";
    private static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    WTDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Create weight_log, user, and goal tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryLog = "CREATE TABLE " + TABLE_LOG +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_WEIGHT + " INTEGER, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_EMAIL + "));";
        db.execSQL(queryLog);

        String queryUser = "CREATE TABLE " + TABLE_USER +
                " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT UNIQUE, " +
                COLUMN_USER_PASSWORD + " TEXT);";
        db.execSQL(queryUser);

        String queryGoal = "CREATE TABLE " + TABLE_GOAL +
                " (" + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GOAL_WEIGHT + " INTEGER, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_EMAIL + "));";
        db.execSQL(queryGoal);
    }

    // Drop and recreate existing tables when upgrading schema
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);
        onCreate(db);
    }

    // Add new weight to weight_log table
    void addWeight(String date, int weight, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_USER_EMAIL, email);

        long result = db.insert(TABLE_LOG, null, cv);

        if (result == -1) {
            Toast.makeText(context, "Failed to save new weight", Toast.LENGTH_SHORT).show();
        }

        else {
            Toast.makeText(context, "New weight is saved!", Toast.LENGTH_SHORT).show();
        }
    }

    // Read all weight entries from weight_log table
    Cursor readAllData(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOG + " WHERE "
                + COLUMN_USER_EMAIL + "=? ORDER BY " + COLUMN_ID + " DESC ", new String[]{email});
        return cursor;
    }

    // Add new user to the user_table
    void addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_NAME, name);
        cv.put(COLUMN_USER_EMAIL, email);
        cv.put(COLUMN_USER_PASSWORD, password);

        long result = db.insertWithOnConflict(TABLE_USER, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

        if (result == -1) {
            Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "User info saved!", Toast.LENGTH_SHORT).show();
        }
    }

    // Authenticate existing user by email and password
    Cursor authenticateUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_PASSWORD + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{email});
        return cursor;
    }

    // Set or update weight in goal_table
    void setGoalWeight(int goal_weight, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_GOAL_WEIGHT, goal_weight);
        // Check if goal exists for the user
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GOAL + " WHERE "
                + COLUMN_USER_EMAIL + "=?", new String[]{email});
        if (cursor.moveToFirst()) {
            // Update existing goal
            int result = db.update(TABLE_GOAL, cv, COLUMN_USER_EMAIL + "=?", new String[]{email});
            if (result == -1) {
                Toast.makeText(context, "Failed to update goal weight", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "Goal weight updated!", Toast.LENGTH_SHORT).show();
            }
        }
        // Insert new goal
        else {
            cv.put(COLUMN_USER_EMAIL, email);
             // Update weight goal value in the database
            long result = db.insert(TABLE_GOAL, null, cv);

            if (result == -1) {
                Toast.makeText(context, "Failed to save goal weight", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "Goal weight added!", Toast.LENGTH_SHORT).show();
            }
        }
        cursor.close();
    }

    // Read the goal weight from goal_table
    Cursor readGoalWeight(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GOAL_WEIGHT + " FROM " + TABLE_GOAL
                + " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{email});
        return cursor;
    }

    // Read the goal weight from weight_log table
    Cursor readCurrentWeight(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_WEIGHT + " FROM " + TABLE_LOG
                + " WHERE " + COLUMN_USER_EMAIL + "=? ORDER BY " + COLUMN_ID + " DESC LIMIT 1", new String[]{email} );
        return cursor;
    }

    // Read the name from user table
    Cursor readUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_NAME +" FROM " + TABLE_USER
                + " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{email});
        return cursor;
    }

    // Remove weight entry from database
    void deleteWeightEntry(int id, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_LOG,  COLUMN_USER_EMAIL + "=? AND " +
                COLUMN_ID + "=?", new String[]{email, String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Entry deleted!", Toast.LENGTH_SHORT).show();
        }
    }

    // Update weight entry
    void updateWeightEntry(int id, String date, int weight, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_WEIGHT, weight);

        int result = db.update(TABLE_LOG, cv, COLUMN_USER_EMAIL + "=? AND " +
                COLUMN_ID + "=?", new String[]{email, String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context, "Failed to update entry", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Entry updated!", Toast.LENGTH_SHORT).show();
        }
    }
}
