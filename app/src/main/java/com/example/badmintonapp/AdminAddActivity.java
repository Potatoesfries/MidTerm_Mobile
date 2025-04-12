package com.example.badmintonapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminAddActivity extends AppCompatActivity {
    private static final String TAG = "AdminAddActivity";
    private EditText subjectEditText, descriptionEditText, priceEditText;
    private Spinner categorySpinner;
    private Button addRecordBtn;
    private AdminDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        try {
            // Set title
            setTitle("Add New Item");

            // Initialize views
            subjectEditText = findViewById(R.id.subject_edittext);
            descriptionEditText = findViewById(R.id.description_edittext);
            priceEditText = findViewById(R.id.price_edittext);
            categorySpinner = findViewById(R.id.category_spinner);
            addRecordBtn = findViewById(R.id.add_record_btn);

            // Check if views were found
            if (subjectEditText == null) {
                Log.e(TAG, "Subject EditText not found");
                Toast.makeText(this, "Layout error: Subject field not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (descriptionEditText == null) {
                Log.e(TAG, "Description EditText not found");
                Toast.makeText(this, "Layout error: Description field not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (priceEditText == null) {
                Log.e(TAG, "Price EditText not found");
                Toast.makeText(this, "Layout error: Price field not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (categorySpinner == null) {
                Log.e(TAG, "Category Spinner not found");
                Toast.makeText(this, "Layout error: Category dropdown not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (addRecordBtn == null) {
                Log.e(TAG, "Add Record Button not found");
                Toast.makeText(this, "Layout error: Add button not found", Toast.LENGTH_LONG).show();
                return;
            }

            // Initialize database helper
            databaseHelper = new AdminDatabaseHelper(this);

            // Load categories into spinner
            loadCategories();

            // Set click listener for add button
            addRecordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveRecord();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in saveRecord", e);
                        Toast.makeText(AdminAddActivity.this,
                                "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Initialization error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
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

            Log.d(TAG, "Spinner adapter set with " + adapter.getCount() + " items");
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

            Toast.makeText(this, "Error loading categories: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecord() {
        try {
            String subject = subjectEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String priceString = priceEditText.getText().toString().trim();

            // Validate input
            if (subject.isEmpty()) {
                subjectEditText.setError("Subject cannot be empty");
                return;
            }

            // Parse and validate price
            double price = 0.0;
            try {
                if (!priceString.isEmpty()) {
                    price = Double.parseDouble(priceString);
                    if (price < 0) {
                        priceEditText.setError("Price cannot be negative");
                        return;
                    }
                } else {
                    priceEditText.setError("Price cannot be empty");
                    return;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Invalid price format");
                return;
            }

            // Make sure we have a valid category
            String category;
            if (categorySpinner.getSelectedItem() == null) {
                category = "Racket"; // Default
                Log.w(TAG, "No category selected, using Racket");
            } else {
                category = categorySpinner.getSelectedItem().toString();
            }

            // Add the item to the database
            long result = databaseHelper.addItem(subject, description, category, price);

            if (result > 0) {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                // Clear fields
                subjectEditText.setText("");
                descriptionEditText.setText("");
                priceEditText.setText("");
                // Reset spinner
                if (categorySpinner.getAdapter() != null &&
                        categorySpinner.getAdapter().getCount() > 0) {
                    categorySpinner.setSelection(0);
                }

                // Return to previous screen
                finish();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving record", e);
            Toast.makeText(this, "Error saving: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No need to close the database helper here as it's managed by the system
    }
}