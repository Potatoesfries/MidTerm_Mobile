package com.example.badmintonapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AdminDBManager {
    private static final String TAG = "AdminDBManager";

    private AdminDatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public AdminDBManager(Context c) {
        context = c;
    }

    public AdminDBManager open() throws SQLException {
        dbHelper = new AdminDatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
            // Check if database has data, if not, initialize it
            if (!hasData()) {
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error opening database: " + e.getMessage());
            throw e;
        }
        return this;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Helper method to check if database has data
    private boolean hasData() {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT COUNT(*) FROM " + AdminDatabaseHelper.TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count > 0;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if database has data: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Insert sample data if the database is empty

    // Insert with default category and price
    public long insert(String name, String desc) throws SQLException {
        return insert(name, desc, "Racket", 0.0); // Default category and price
    }

    // Insert with category but default price
    public long insert(String name, String desc, String category) throws SQLException {
        return insert(name, desc, category, 0.0); // Default price
    }

    // Insert with all parameters
    public long insert(String name, String desc, String category, double price) throws SQLException {
        ContentValues contentValue = new ContentValues();
        contentValue.put(AdminDatabaseHelper.SUBJECT, name);
        contentValue.put(AdminDatabaseHelper.DESC, desc);
        contentValue.put(AdminDatabaseHelper.CATEGORY, category);
        contentValue.put(AdminDatabaseHelper.PRICE, price);

        try {
            return database.insertOrThrow(AdminDatabaseHelper.TABLE_NAME, null, contentValue);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting data: " + e.getMessage());
            throw e;
        }
    }

    public Cursor fetch() throws SQLException {
        String[] columns = new String[] {
                AdminDatabaseHelper._ID,
                AdminDatabaseHelper.SUBJECT,
                AdminDatabaseHelper.DESC,
                AdminDatabaseHelper.CATEGORY,
                AdminDatabaseHelper.PRICE // Include price column
        };

        try {
            Cursor cursor = database.query(
                    AdminDatabaseHelper.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    AdminDatabaseHelper.SUBJECT + " ASC" // Sort by name
            );

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Log.d(TAG, "Fetched " + cursor.getCount() + " items");

                    // Log the first item for debugging
                    if (!cursor.isAfterLast()) {
                        int idIdx = cursor.getColumnIndex(AdminDatabaseHelper._ID);
                        int subjIdx = cursor.getColumnIndex(AdminDatabaseHelper.SUBJECT);

                        if (idIdx >= 0 && subjIdx >= 0) {
                            Log.d(TAG, "First item: ID=" + cursor.getLong(idIdx) +
                                    ", Subject=" + cursor.getString(subjIdx));
                        }
                    }
                } else {
                    Log.w(TAG, "No items found in database");
                }
            }
            return cursor;
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching data: " + e.getMessage());
            throw e;
        }
    }

    public Cursor fetchByCategory(String category) throws SQLException {
        String[] columns = new String[] {
                AdminDatabaseHelper._ID,
                AdminDatabaseHelper.SUBJECT,
                AdminDatabaseHelper.DESC,
                AdminDatabaseHelper.CATEGORY,
                AdminDatabaseHelper.PRICE
        };

        try {
            String selection = null;
            String[] selectionArgs = null;

            if (category != null && !category.equalsIgnoreCase("all")) {
                selection = AdminDatabaseHelper.CATEGORY + " = ?";
                selectionArgs = new String[] { category };
            }

            Cursor cursor = database.query(
                    AdminDatabaseHelper.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    AdminDatabaseHelper.SUBJECT + " ASC"
            );

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                }
                Log.d(TAG, "Fetched " + cursor.getCount() + " items for category: " + category);
            }
            return cursor;
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching data by category: " + e.getMessage());
            throw e;
        }
    }

    // Update with default price (gets the current price)
    public int update(long _id, String name, String desc) throws SQLException {
        // Get current category and price before updating
        String category = "Racket"; // Default
        double price = 0.0; // Default
        Cursor cursor = getItemById(_id);
        if (cursor != null && cursor.moveToFirst()) {
            int categoryColumnIndex = cursor.getColumnIndex(AdminDatabaseHelper.CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(AdminDatabaseHelper.PRICE);

            if (categoryColumnIndex != -1) {
                category = cursor.getString(categoryColumnIndex);
            }

            if (priceColumnIndex != -1) {
                price = cursor.getDouble(priceColumnIndex);
            }

            cursor.close();
        }

        return update(_id, name, desc, category, price);
    }

    // Update with category but using existing price
    public int update(long _id, String name, String desc, String category) throws SQLException {
        double price = 0.0; // Default
        Cursor cursor = getItemById(_id);
        if (cursor != null && cursor.moveToFirst()) {
            int priceColumnIndex = cursor.getColumnIndex(AdminDatabaseHelper.PRICE);

            if (priceColumnIndex != -1) {
                price = cursor.getDouble(priceColumnIndex);
            }

            cursor.close();
        }

        return update(_id, name, desc, category, price);
    }

    // Update with all parameters
    public int update(long _id, String name, String desc, String category, double price) throws SQLException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AdminDatabaseHelper.SUBJECT, name);
        contentValues.put(AdminDatabaseHelper.DESC, desc);
        contentValues.put(AdminDatabaseHelper.CATEGORY, category);
        contentValues.put(AdminDatabaseHelper.PRICE, price);

        try {
            int result = database.update(
                    AdminDatabaseHelper.TABLE_NAME,
                    contentValues,
                    AdminDatabaseHelper._ID + " = ?",
                    new String[]{String.valueOf(_id)}
            );
            Log.d(TAG, "Updated item with ID " + _id + ", rows affected: " + result);
            return result;
        } catch (SQLException e) {
            Log.e(TAG, "Error updating data: " + e.getMessage());
            throw e;
        }
    }

    public void delete(long _id) throws SQLException {
        try {
            int result = database.delete(
                    AdminDatabaseHelper.TABLE_NAME,
                    AdminDatabaseHelper._ID + " = ?",
                    new String[]{String.valueOf(_id)}
            );
            Log.d(TAG, "Deleted item with ID " + _id + ", rows affected: " + result);
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting data: " + e.getMessage());
            throw e;
        }
    }

    // Get a single item by ID
    public Cursor getItemById(long id) {
        String[] columns = new String[] {
                AdminDatabaseHelper._ID,
                AdminDatabaseHelper.SUBJECT,
                AdminDatabaseHelper.DESC,
                AdminDatabaseHelper.CATEGORY,
                AdminDatabaseHelper.PRICE
        };

        Cursor cursor = database.query(
                AdminDatabaseHelper.TABLE_NAME,
                columns,
                AdminDatabaseHelper._ID + " = ?",
                new String[] { String.valueOf(id) },
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d(TAG, "Retrieved item with ID " + id);
        }

        return cursor;
    }

    // Update search to include category and price
    public Cursor searchItems(String searchText) {
        String[] columns = new String[] {
                AdminDatabaseHelper._ID,
                AdminDatabaseHelper.SUBJECT,
                AdminDatabaseHelper.DESC,
                AdminDatabaseHelper.CATEGORY,
                AdminDatabaseHelper.PRICE
        };

        // Create a LIKE query for partial matches
        String selection = AdminDatabaseHelper.SUBJECT + " LIKE ? OR " +
                AdminDatabaseHelper.DESC + " LIKE ?";
        String[] selectionArgs = new String[] {
                "%" + searchText + "%",
                "%" + searchText + "%"
        };

        // Perform the query
        Cursor cursor = database.query(
                AdminDatabaseHelper.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d(TAG, "Search found " + cursor.getCount() + " results for: " + searchText);
        }

        return cursor;
    }
}
