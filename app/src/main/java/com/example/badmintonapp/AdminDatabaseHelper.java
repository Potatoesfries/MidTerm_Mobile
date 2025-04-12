package com.example.badmintonapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "AdminDatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "BadmintonShopDB";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_NAME = "products";
    public static final String CATEGORIES_TABLE = "categories"; // Added categories table

    // Column names
    public static final String _ID = "_id";
    public static final String SUBJECT = "subject";
    public static final String DESC = "desc";
    public static final String CATEGORY = "category";
    public static final String PRICE = "price";

    // Categories table columns
    public static final String CATEGORY_ID = "_id";
    public static final String CATEGORY_NAME = "name";

    // Default categories
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList("Racket", "Shuttlecock", "Footwear");

    // Table creation SQL statement
    private static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SUBJECT + " TEXT NOT NULL, " +
            DESC + " TEXT, " +
            CATEGORY + " TEXT DEFAULT 'Racket', " +
            PRICE + " REAL DEFAULT 0.0" +
            ");";

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + CATEGORIES_TABLE + " (" +
            CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CATEGORY_NAME + " TEXT UNIQUE NOT NULL" +
            ");";

    public AdminDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create both tables
            db.execSQL(CREATE_PRODUCTS_TABLE);
            db.execSQL(CREATE_CATEGORIES_TABLE);

            // Insert default categories
            insertDefaultCategories(db);

            // Add some initial product data
            insertInitialData(db);

            Log.d(TAG, "Database tables created");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop the existing tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        try {
            for (String category : DEFAULT_CATEGORIES) {
                ContentValues values = new ContentValues();
                values.put(CATEGORY_NAME, category);
                db.insertWithOnConflict(CATEGORIES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            Log.d(TAG, "Default categories inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default categories: " + e.getMessage(), e);
        }
    }

    private void insertInitialData(SQLiteDatabase db) {
        try {
            // Insert some example items - Racket category
            ContentValues values1 = new ContentValues();
            values1.put(SUBJECT, "Yonex Astrox 88D Pro");
            values1.put(DESC, "Professional badminton racket for advanced players, used by many international players");
            values1.put(CATEGORY, "Racket");
            values1.put(PRICE, 199.99);
            db.insert(TABLE_NAME, null, values1);

            ContentValues values2 = new ContentValues();
            values2.put(SUBJECT, "Victor Thruster K9000");
            values2.put(DESC, "Lightweight badminton racket for beginners with excellent control");
            values2.put(CATEGORY, "Racket");
            values2.put(PRICE, 89.99);
            db.insert(TABLE_NAME, null, values2);

            ContentValues values3 = new ContentValues();
            values3.put(SUBJECT, "Li-Ning N7 II");
            values3.put(DESC, "High-performance badminton racket with excellent durability");
            values3.put(CATEGORY, "Racket");
            values3.put(PRICE, 149.50);
            db.insert(TABLE_NAME, null, values3);

            // Add Shuttlecock category items
            ContentValues values4 = new ContentValues();
            values4.put(SUBJECT, "Yonex Aerosensa 30");
            values4.put(DESC, "Professional grade feather shuttlecock, tournament standard");
            values4.put(CATEGORY, "Shuttlecock");
            values4.put(PRICE, 29.99);
            db.insert(TABLE_NAME, null, values4);

            ContentValues values5 = new ContentValues();
            values5.put(SUBJECT, "Carlton T800 Training");
            values5.put(DESC, "Durable nylon shuttlecock for daily practice, pack of 12");
            values5.put(CATEGORY, "Shuttlecock");
            values5.put(PRICE, 15.75);
            db.insert(TABLE_NAME, null, values5);

            // Add Footwear category
            ContentValues values6 = new ContentValues();
            values6.put(SUBJECT, "Yonex Power Cushion 65 Z2");
            values6.put(DESC, "Professional badminton shoes with excellent grip and comfort");
            values6.put(CATEGORY, "Footwear");
            values6.put(PRICE, 129.99);
            db.insert(TABLE_NAME, null, values6);

            Log.d(TAG, "Initial data inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting initial data: " + e.getMessage(), e);
        }
    }

    // Method to get all unique category names from the database
    public List<String> getAllCategoryNames() {
        List<String> categories = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            // First try to get categories from the dedicated categories table
            Cursor categoryCursor = db.query(
                    CATEGORIES_TABLE,
                    new String[]{CATEGORY_NAME},
                    null, null, null, null, CATEGORY_NAME + " ASC");

            if (categoryCursor != null && categoryCursor.moveToFirst()) {
                do {
                    String category = categoryCursor.getString(0);
                    if (category != null && !category.isEmpty()) {
                        categories.add(category);
                    }
                } while (categoryCursor.moveToNext());
                categoryCursor.close();

                Log.d(TAG, "Found " + categories.size() + " categories from categories table");
            } else {
                Log.d(TAG, "No categories found in categories table");

                // If no categories in dedicated table, fallback to getting distinct categories from products
                Cursor cursor = db.query(true, TABLE_NAME,
                        new String[]{CATEGORY},
                        null, null, CATEGORY, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int categoryIndex = cursor.getColumnIndex(CATEGORY);

                    if (categoryIndex >= 0) {
                        // Collect all categories from database
                        Set<String> uniqueCategories = new HashSet<>();
                        do {
                            String category = cursor.getString(categoryIndex);
                            if (category != null && !category.isEmpty()) {
                                uniqueCategories.add(category);
                            }
                        } while (cursor.moveToNext());

                        // Add all unique categories to our result list
                        categories.addAll(uniqueCategories);
                        Log.d(TAG, "Found " + categories.size() + " categories from products table");
                    }
                    cursor.close();
                }
            }

            // If still no categories found, add the defaults
            if (categories.isEmpty()) {
                Log.d(TAG, "No categories found in database, adding defaults");
                categories.addAll(DEFAULT_CATEGORIES);

                // And also insert them into the categories table for future use
                SQLiteDatabase writeDb = this.getWritableDatabase();
                insertDefaultCategories(writeDb);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category names: " + e.getMessage(), e);
            // Add defaults on error
            categories.addAll(DEFAULT_CATEGORIES);
        }

        return categories;
    }

    // Method to add a new category
    public long addCategory(String categoryName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CATEGORY_NAME, categoryName);
            return db.insertWithOnConflict(CATEGORIES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } catch (Exception e) {
            Log.e(TAG, "Error adding category: " + e.getMessage(), e);
            return -1;
        }
    }

    // Add a new item to the database - now with price parameter
    public long addItem(String subject, String description, String category, double price) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            // First ensure the category exists in the categories table
            addCategory(category);

            ContentValues values = new ContentValues();
            values.put(SUBJECT, subject);
            values.put(DESC, description);
            values.put(CATEGORY, category);
            values.put(PRICE, price);

            long result = db.insert(TABLE_NAME, null, values);
            Log.d(TAG, "Item added with ID: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding item: " + e.getMessage(), e);
            return -1;
        }
    }

    // Update an existing item - now with price parameter
    public int updateItem(long id, String subject, String description, String category, double price) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            // First ensure the category exists in the categories table
            addCategory(category);

            ContentValues values = new ContentValues();
            values.put(SUBJECT, subject);
            values.put(DESC, description);
            values.put(CATEGORY, category);
            values.put(PRICE, price);

            int result = db.update(TABLE_NAME, values, _ID + " = ?", new String[]{String.valueOf(id)});
            Log.d(TAG, "Updated item with ID " + id + ", rows affected: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error updating item: " + e.getMessage(), e);
            return 0;
        }
    }

    // Delete an item
    public int deleteItem(long id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int result = db.delete(TABLE_NAME, _ID + " = ?", new String[]{String.valueOf(id)});
            Log.d(TAG, "Deleted item with ID " + id + ", rows affected: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting item: " + e.getMessage(), e);
            return 0;
        }
    }

    // Get a single item by ID
    public Cursor getItemById(long id) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[]{_ID, SUBJECT, DESC, CATEGORY, PRICE},
                    _ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                Log.d(TAG, "Retrieved item with ID " + id);
            } else {
                Log.e(TAG, "Failed to retrieve item with ID " + id);
            }

            return cursor;
        } catch (Exception e) {
            Log.e(TAG, "Error getting item by ID: " + e.getMessage(), e);
            return null;
        }
    }

    // Get all items
    public Cursor getAllItems() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[]{_ID, SUBJECT, DESC, CATEGORY, PRICE},
                    null, null, null, null,
                    SUBJECT + " ASC");

            if (cursor != null) {
                cursor.moveToFirst();
                Log.d(TAG, "Retrieved " + cursor.getCount() + " items");
            } else {
                Log.e(TAG, "Failed to retrieve items");
            }

            return cursor;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all items: " + e.getMessage(), e);
            return null;
        }
    }

    // Check if database has data
    public boolean hasData() {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

            boolean hasData = false;
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                hasData = count > 0;
                Log.d(TAG, "Database has " + count + " items");
                cursor.close();
            }

            return hasData;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if database has data: " + e.getMessage(), e);
            return false;
        }
    }
}