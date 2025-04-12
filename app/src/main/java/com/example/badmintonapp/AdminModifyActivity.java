package com.example.badmintonapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminModifyActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "AdminModifyActivity";
    private EditText titleText;
    private EditText descText;
    private EditText priceText;
    private Button updateBtn, deleteBtn;
    private Spinner categorySpinner;
    private long _id;
    private AdminDBManager dbManager;
    private AdminDatabaseHelper databaseHelper;
    private String originalCategory;
    private double originalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Modify Item");
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_admin_modify);

        // Initialize DB manager
        dbManager = new AdminDBManager(this);
        dbManager.open();

        // Initialize database helper for category operations
        databaseHelper = new AdminDatabaseHelper(this);

        // Initialize UI components
        titleText = findViewById(R.id.subject_edittext);
        descText = findViewById(R.id.description_edittext);
        priceText = findViewById(R.id.price_edittext);
        categorySpinner = findViewById(R.id.category_spinner);
        updateBtn = findViewById(R.id.btn_update);
        deleteBtn = findViewById(R.id.btn_delete);

        // Get intent data
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        originalCategory = intent.getStringExtra("category");
        originalPrice = intent.getDoubleExtra("price", 0.0);

        // Validate intent data
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Error: No item ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set values
        _id = Long.parseLong(id);
        titleText.setText(name);
        descText.setText(desc);
        priceText.setText(String.valueOf(originalPrice));

        // If category isn't in intent, query the database to get it
        if (originalCategory == null || originalCategory.isEmpty()) {
            Cursor itemCursor = dbManager.getItemById(_id);
            if (itemCursor != null && itemCursor.moveToFirst()) {
                int categoryIdx = itemCursor.getColumnIndex(AdminDatabaseHelper.CATEGORY);
                int priceIdx = itemCursor.getColumnIndex(AdminDatabaseHelper.PRICE);

                if (categoryIdx >= 0) {
                    originalCategory = itemCursor.getString(categoryIdx);
                }

                if (priceIdx >= 0) {
                    originalPrice = itemCursor.getDouble(priceIdx);
                    priceText.setText(String.valueOf(originalPrice));
                }

                itemCursor.close();
            }

            // If still null, set default
            if (originalCategory == null || originalCategory.isEmpty()) {
                originalCategory = "Racket";
            }
        }

        // Load categories into spinner
        loadCategories();

        // Set click listeners
        updateBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
    }

    private void loadCategories() {
        try {
            List<String> categories = databaseHelper.getAllCategoryNames();

            Log.d(TAG, "Loading categories: " + categories.toString());

            // Make sure we have at least one category
            if (categories == null || categories.isEmpty()) {
                categories = new ArrayList<>();
                categories.add("Racket");
                categories.add("Shuttlecock");
                categories.add("Footwear");
                Log.w(TAG, "No categories found, using defaults");
            }

            // Create adapter for spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set adapter to spinner
            categorySpinner.setAdapter(adapter);

            // Set the selected category if it exists
            if (originalCategory != null && !originalCategory.isEmpty()) {
                int position = adapter.getPosition(originalCategory);
                if (position >= 0) {
                    categorySpinner.setSelection(position);
                    Log.d(TAG, "Selected category position: " + position);
                } else {
                    Log.w(TAG, "Category " + originalCategory + " not found in adapter");
                    // Add the category if it doesn't exist in the list
                    adapter.add(originalCategory);
                    position = adapter.getPosition(originalCategory);
                    categorySpinner.setSelection(position);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories", e);

            // Create a fallback adapter with default categories
            List<String> fallbackCategories = new ArrayList<>();
            fallbackCategories.add("Racket");
            fallbackCategories.add("Shuttlecock");
            fallbackCategories.add("Footwear");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, fallbackCategories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            categorySpinner.setAdapter(adapter);

            // Try to select the original category
            if (originalCategory != null && !originalCategory.isEmpty()) {
                int position = adapter.getPosition(originalCategory);
                if (position >= 0) {
                    categorySpinner.setSelection(position);
                }
            }

            Toast.makeText(this, "Error loading categories: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_update) {
            String title = titleText.getText().toString().trim();
            String desc = descText.getText().toString().trim();
            String priceString = priceText.getText().toString().trim();

            // Get selected category
            String category;
            if (categorySpinner.getSelectedItem() == null) {
                category = "Racket"; // Default if nothing selected
            } else {
                category = categorySpinner.getSelectedItem().toString();
            }

            // Validate input
            if (title.isEmpty()) {
                titleText.setError("Name cannot be empty");
                return;
            }

            // Parse and validate price
            double price = 0.0;
            try {
                if (!priceString.isEmpty()) {
                    price = Double.parseDouble(priceString);
                    if (price < 0) {
                        priceText.setError("Price cannot be negative");
                        return;
                    }
                } else {
                    priceText.setError("Price cannot be empty");
                    return;
                }
            } catch (NumberFormatException e) {
                priceText.setError("Invalid price format");
                return;
            }

            try {
                // Update the item
                int result = databaseHelper.updateItem(_id, title, desc, category, price);

                if (result > 0) {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    this.returnHome();
                } else {
                    // Fallback to the DBManager's update method if the databaseHelper update fails
                    result = dbManager.update(_id, title, desc, category, price);

                    if (result > 0) {
                        Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        this.returnHome();
                    } else {
                        Toast.makeText(this, "Error updating item: No rows affected",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating item: " + e.getMessage(), e);
                Toast.makeText(this, "Error updating item: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btn_delete) {
            // Add confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            dbManager.delete(_id);
                            Toast.makeText(this, "Item deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                            this.returnHome();
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting item: " + e.getMessage(), e);
                            Toast.makeText(this, "Error deleting item: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    public void returnHome() {
        Intent home_intent = new Intent(getApplicationContext(),
                Admin.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home_intent);
        finish(); // Proper Activity cleanup
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (dbManager != null) {
            dbManager.close();
        }
    }
}