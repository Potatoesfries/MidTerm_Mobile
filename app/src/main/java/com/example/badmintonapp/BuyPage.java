package com.example.badmintonapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BuyPage extends AppCompatActivity {
    private static final String TAG = "BuyPage";
    private Button cancelButton;
    private Button confirmButton;
    private TextView itemNameTextView;
    private TextView itemDescriptionTextView;
    private TextView itemCategoryTextView;
    private TextView itemPriceTextView;
    private ImageView buyPageImageView;
    private ScrollView invoiceScrollView;
    private TextView invoiceTextView;

    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private double itemPrice;
    private int itemImage;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Hide the action bar to prevent title overlay
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_buy_page);

        // Initialize views
        setupViews();

        // Set window insets to handle edge-to-edge display properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        getIntentData();

        // Set data to views
        populateViews();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void setupViews() {
        try {
            cancelButton = findViewById(R.id.cancelButton);
            confirmButton = findViewById(R.id.confirmButton);
            itemNameTextView = findViewById(R.id.itemNameTextView);
            itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);
            itemCategoryTextView = findViewById(R.id.categoryTextView);
            itemPriceTextView = findViewById(R.id.priceTextView);
            buyPageImageView = findViewById(R.id.buyPageImageView);
            invoiceScrollView = findViewById(R.id.invoiceScrollView);
            invoiceTextView = findViewById(R.id.invoiceTextView);

            // Initially hide invoice
            if (invoiceScrollView != null) {
                invoiceScrollView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getIntentData() {
        try {
            Intent intent = getIntent();

            if (intent != null) {
                itemName = intent.getStringExtra("itemName");
                itemDescription = intent.getStringExtra("itemDescription");
                itemCategory = intent.getStringExtra("itemCategory");
                itemPrice = intent.getDoubleExtra("itemPrice", 0.0);
                itemImage = intent.getIntExtra("itemImage", R.drawable.racket);
                itemId = intent.getLongExtra("itemId", -1);

                // Set defaults if needed
                if (itemName == null) itemName = "Unknown Item";
                if (itemDescription == null) itemDescription = "No description available";
                if (itemCategory == null || itemCategory.isEmpty()) itemCategory = "Racket";
            } else {
                // Handle missing intent data
                Log.e(TAG, "No intent data provided");
                Toast.makeText(this, "Error: No product data available", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting intent data: " + e.getMessage());
            Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateViews() {
        try {
            itemNameTextView.setText(itemName);
            itemDescriptionTextView.setText(itemDescription);
            itemCategoryTextView.setText("Category: " + itemCategory);

            // Format price as currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            itemPriceTextView.setText("Price: " + currencyFormat.format(itemPrice));

            buyPageImageView.setImageResource(itemImage);
        } catch (Exception e) {
            Log.e(TAG, "Error populating views: " + e.getMessage());
        }
    }

    private void setupButtonListeners() {
        // Set up cancel button with safe handling
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Safely return to previous screen
                finish();
            }
        });

        // Set up confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Generate and display invoice
                    generateInvoice();

                    // Change button text
                    confirmButton.setText("Return to Home");
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Return to main activity
                            Intent intent = new Intent(BuyPage.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });

                    // Hide cancel button
                    cancelButton.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing purchase: " + e.getMessage());
                    Toast.makeText(BuyPage.this, "Error processing purchase", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateInvoice() {
        try {
            // Get current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date());

            // Generate a unique order ID
            String orderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Calculate prices
            double tax = itemPrice * 0.07; // 7% tax
            double shipping = 8.99;  // Fixed shipping cost
            double total = itemPrice + tax + shipping;

            // Format currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

            // Format the invoice
            StringBuilder invoiceBuilder = new StringBuilder();
            invoiceBuilder.append("INVOICE\n\n");
            invoiceBuilder.append("Order ID: ").append(orderId).append("\n");
            invoiceBuilder.append("Date: ").append(currentDateTime).append("\n\n");
            invoiceBuilder.append("Item: ").append(itemName).append("\n");
            invoiceBuilder.append("Category: ").append(itemCategory).append("\n");
            invoiceBuilder.append("Description: ").append(itemDescription).append("\n\n");
            invoiceBuilder.append("Price: ").append(currencyFormat.format(itemPrice)).append("\n");
            invoiceBuilder.append("Tax (7%): ").append(currencyFormat.format(tax)).append("\n");
            invoiceBuilder.append("Shipping: ").append(currencyFormat.format(shipping)).append("\n");
            invoiceBuilder.append("Total: ").append(currencyFormat.format(total)).append("\n\n");
            invoiceBuilder.append("Thank you for your purchase!");

            // Display the invoice
            invoiceTextView.setText(invoiceBuilder.toString());
            invoiceScrollView.setVisibility(View.VISIBLE);

            // Make a toast notification
            Toast.makeText(this, "Purchase completed! Thank you for shopping with us.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error generating invoice: " + e.getMessage());
            Toast.makeText(this, "Error generating invoice", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Simply finish activity to return to previous screen safely
        finish();
    }
}