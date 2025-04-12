package com.example.badmintonapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Admin extends AppCompatActivity {
    private static final String TAG = "Admin";
    private AdminDBManager dbManager;
    private ListView listView;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    private Button addNewButton;
    private Button viewCustomerButton;

    // Make sure these match your columns and view IDs exactly
    final String[] from = new String[]{
            AdminDatabaseHelper._ID,
            AdminDatabaseHelper.SUBJECT,
            AdminDatabaseHelper.DESC,
            AdminDatabaseHelper.CATEGORY,
            AdminDatabaseHelper.PRICE
    };

    final int[] to = new int[]{
            R.id.id,
            R.id.title,
            R.id.desc,
            R.id.category,
            R.id.price
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting window insets: " + e.getMessage());
        }

        // Set activity title
        setTitle("Admin Panel");

        // Initialize database manager
        dbManager = new AdminDBManager(this);
        dbManager.open();

        // Initialize UI components
        listView = findViewById(R.id.list_view);
        addNewButton = findViewById(R.id.add_button);
        viewCustomerButton = findViewById(R.id.view_customer_button);

        // Check if UI components were found
        if (listView == null) {
            Log.e(TAG, "ListView not found in layout");
            Toast.makeText(this, "Error: Could not initialize item list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addNewButton == null || viewCustomerButton == null) {
            Log.e(TAG, "One or more buttons not found in layout");
            Toast.makeText(this, "Error: Some UI controls are missing", Toast.LENGTH_SHORT).show();
        }

        // Set empty view for list
        View emptyView = findViewById(R.id.empty);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }

        // Load data
        loadData();

        // Set click listener for add button
        if (addNewButton != null) {
            addNewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addIntent = new Intent(Admin.this, AdminAddActivity.class);
                    startActivity(addIntent);
                }
            });
        }

        // Set click listener for view customer button
        if (viewCustomerButton != null) {
            viewCustomerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent customerIntent = new Intent(Admin.this, MainActivity.class);
                    startActivity(customerIntent);
                }
            });
        }

        // Set click listener for list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                try {
                    TextView idTextView = view.findViewById(R.id.id);
                    TextView titleTextView = view.findViewById(R.id.title);
                    TextView descTextView = view.findViewById(R.id.desc);
                    TextView categoryTextView = view.findViewById(R.id.category);
                    TextView priceTextView = view.findViewById(R.id.price);

                    if (idTextView == null || titleTextView == null || descTextView == null) {
                        Log.e(TAG, "Required TextViews not found in item view");
                        Toast.makeText(Admin.this, "Error: Could not access item details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = idTextView.getText().toString();
                    String title = titleTextView.getText().toString();
                    String desc = descTextView.getText().toString();
                    String category = categoryTextView != null ? categoryTextView.getText().toString() : "Racket";
                    String priceText = priceTextView != null ? priceTextView.getText().toString().replace("$", "") : "0.0";
                    double price = 0.0;

                    try {
                        price = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing price: " + e.getMessage());
                    }

                    Intent modifyIntent = new Intent(getApplicationContext(), AdminModifyActivity.class);
                    modifyIntent.putExtra("title", title);
                    modifyIntent.putExtra("desc", desc);
                    modifyIntent.putExtra("id", id);
                    modifyIntent.putExtra("category", category);
                    modifyIntent.putExtra("price", price);
                    startActivity(modifyIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error handling item click: " + e.getMessage());
                    Toast.makeText(Admin.this, "Error accessing item details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadData();
    }

    private void loadData() {
        try {
            // Close any existing cursor to prevent memory leaks
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            // Fetch the cursor
            cursor = dbManager.fetch();

            // Check if we got a valid cursor
            if (cursor == null) {
                Log.e(TAG, "Cursor is null in loadData()");
                Toast.makeText(this, "Error: Could not retrieve data from database",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Create adapter with layout that matches our views
            adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.item_view,
                    cursor,
                    from,
                    to,
                    0
            );

            // Set the ViewBinder to customize how data is displayed
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    // Set category icon based on category value
                    if (view.getId() == R.id.category_icon) {
                        String category = cursor.getString(cursor.getColumnIndexOrThrow(AdminDatabaseHelper.CATEGORY));
                        ImageView imageView = (ImageView) view;

                        switch (category.toLowerCase()) {
                            case "racket":
                                imageView.setImageResource(R.drawable.ic_racket);
                                break;
                            case "shuttlecock":
                                imageView.setImageResource(R.drawable.ic_shuttlecock);
                                break;
                            default:
                                imageView.setImageResource(R.drawable.ic_accessories);
                                break;
                        }
                        return true;
                    }

                    // Format price with dollar sign
                    if (view.getId() == R.id.price) {
                        if (columnIndex == cursor.getColumnIndexOrThrow(AdminDatabaseHelper.PRICE)) {
                            double price = cursor.getDouble(columnIndex);
                            TextView textView = (TextView) view;
                            textView.setText("$" + String.format("%.2f", price));
                            return true;
                        }
                    }

                    return false;
                }
            });

            // Set adapter to list view
            listView.setAdapter(adapter);

            // Log cursor count for debugging
            Log.d(TAG, "Cursor contains " + cursor.getCount() + " items");

        } catch (Exception e) {
            Log.e(TAG, "Error loading data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close cursor and database
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (dbManager != null) {
            dbManager.close();
        }
    }
}