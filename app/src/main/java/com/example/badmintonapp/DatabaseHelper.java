package com.example.badmintonapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDB";
    private static final int DATABASE_VERSION = 1;

    // User table
    private static final String TABLE_NAME = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_EMAIL = "email"; // Added email column
    private static final String COL_PASSWORD = "password";

    private static final String TAG = "DatabaseHelper"; // For logging

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table with email column
        String createUsersTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT, " +
                COL_EMAIL + " TEXT, " + // Added email column
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Add demo user
        addDemoUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void addDemoUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, "user");
        values.put(COL_EMAIL, "user@example.com"); // Default email
        values.put(COL_PASSWORD, "password");
        db.insert(TABLE_NAME, null, values);

        ContentValues adminValues = new ContentValues();
        adminValues.put(COL_USERNAME, "admin");
        adminValues.put(COL_EMAIL, "admin@example.com"); // Admin email
        adminValues.put(COL_PASSWORD, "admin");
        db.insert(TABLE_NAME, null, adminValues);
    }

    // User authentication methods
    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL, email); // Add email to registration
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1; // Returns true if registration is successful
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists; // Returns true if user exists
    }

    public boolean checkUsername(String username) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_USERNAME + "=?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Method to get user email by username
    public String getUserEmail(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_EMAIL + " FROM " + TABLE_NAME +
                " WHERE " + COL_USERNAME + "=?", new String[]{username});

        String email = "";
        if (cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(0);
            cursor.close();
        }
        return email;
    }
}