package com.padgettanna.weighttracker;

import com.padgettanna.weighttracker.model.WeightEntry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Database helper class for the Weight Tracker app.
 * Responsibilities:
 * - Manages SQLite schema creation and upgrades
 * - Performs CRUD operations for users, goals, and weight entries
 * - Enforces basic validation rules for weight and date values
 * This class intentionally returns boolean results for write operations
 * to allow calling activities to handle validation feedback and UI flow.
 */
public class WTDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    // Validation constraints for weight entries (used by add/update operations)
    private static final int MIN_WEIGHT = 50;
    private static final int MAX_WEIGHT = 999;

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

    /**
     * Inserts a new weight entry for the given user.
     *
     * @return true if the entry was successfully inserted; false if validation fails
     *         or the database insert does not succeed.
     */
    boolean addWeight(String date, int weight, String email) {
        if (!isValidWeight(weight) || !isValidDate(date)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_USER_EMAIL, email);

        return db.insert(TABLE_LOG, null, cv) != -1;
    }

    /**
     * Retrieves all weight entries for a given user and converts them
     * into a list of WeightEntry objects for algorithmic processing.
     */
    public List<WeightEntry> getWeightEntries(String userEmail) {
        List<WeightEntry> entries = new ArrayList<>();

        if (userEmail == null || userEmail.isBlank()) {
            return entries;
        }

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + ", " + COLUMN_DATE + ", " + COLUMN_WEIGHT +
                        " FROM " + TABLE_LOG +
                        " WHERE " + COLUMN_USER_EMAIL + " = ?",
                new String[]{ userEmail }
        );

        while (cursor.moveToNext()) {
            try {
                entries.add(new WeightEntry(
                        cursor.getInt(0),
                        LocalDate.parse(cursor.getString(1)),
                        cursor.getInt(2)
                ));
            } catch (Exception ignored) {}
        }

        cursor.close();
        return entries;
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

    /**
     * Sets or updates the user's goal weight.
     *
     * @return true if the goal was inserted or updated successfully;
     *         false if validation fails or no rows were affected.
     */
    boolean setGoalWeight(int goal_weight, String email) {
        // Validate input before touching the database
        if (!isValidWeight(goal_weight) || email == null || email.isBlank()) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_GOAL_WEIGHT, goal_weight);
        cv.put(COLUMN_USER_EMAIL, email);

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_GOAL + " WHERE " + COLUMN_USER_EMAIL + "=?",
                new String[]{email}
        );

        boolean success;

        if (cursor.moveToFirst()) {
            // Update existing goal
            success = db.update(
                    TABLE_GOAL,
                    cv,
                    COLUMN_USER_EMAIL + "=?",
                    new String[]{email}
            ) > 0;
        } else {
            // Insert new goal
            success = db.insert(TABLE_GOAL, null, cv) != -1;
        }

        cursor.close();
        return success;
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
                + " WHERE " + COLUMN_USER_EMAIL + "=? ORDER BY " + COLUMN_DATE + " DESC, "
                + COLUMN_ID + " DESC " + " LIMIT 1", new String[]{email} );
        return cursor;
    }

    // Read the name from user table
    Cursor readUserName(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_NAME +" FROM " + TABLE_USER
                + " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{email});
        return cursor;
    }

    // Remove weight entry from database
    boolean deleteWeightEntry(int id, String email) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_LOG,  COLUMN_USER_EMAIL + "=? AND " +
                COLUMN_ID + "=?", new String[]{email, String.valueOf(id)}) > 0;
    }

    /**
     * Updates an existing weight entry.
     * Validation is performed before attempting the update.
     * If validation fails, no database operation is executed.
     * @return true if at least one row was updated; false otherwise
     */
    boolean updateWeightEntry(int id, String date, int weight, String email) {
        if (!isValidWeight(weight) || !isValidDate(date)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_WEIGHT, weight);

        int rows = db.update(
                TABLE_LOG,
                cv,
                COLUMN_USER_EMAIL + "=? AND " + COLUMN_ID + "=?",
                new String[]{email, String.valueOf(id)}
        );

        return rows > 0;
    }

    // Ensures weight values remain within realistic human bounds
    private boolean isValidWeight(int weight) {
        return weight >= MIN_WEIGHT && weight <= MAX_WEIGHT;
    }

    // Prevents invalid or future-dated entries from being stored
    private boolean isValidDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return !date.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}
