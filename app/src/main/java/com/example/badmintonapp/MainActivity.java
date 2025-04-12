package com.example.badmintonapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private TextView tabAll, tabRacket, tabShuttlecock, tabFootwear;
    private CircleImageView profileCircle;
    private String currentCategory = "all"; // Default category

    // Properties from BuyPage
    private Button cancelButton;
    private Button confirmButton;
    private TextView itemNameTextView;
    private TextView itemDescriptionTextView;
    private TextView itemCategoryTextView;
    private TextView itemPriceTextView;
    private ImageView buyPageImageView;
    private ScrollView invoiceScrollView;
    private TextView invoiceTextView;

    // Current view state to track which page is displayed
    private enum ViewState {
        MAIN, DETAIL, CHECKOUT
    }

    private ViewState currentState = ViewState.MAIN;

    // Item data for detail and buy pages
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private double itemPrice;
    private int itemImage;
    private long itemId;

    private AdminDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        currentState = ViewState.MAIN;

        // Initialize database manager
        dbManager = new AdminDBManager(this);
        dbManager.open();

        // Set activity title
//        setTitle("Badminton Shop");
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize bottom navigation
        setupMainNavigation();

        // Initialize top navigation
        setupTopNavigation();

        // Only set window insets if the main view exists
        View mainView = findViewById(R.id.fragment_container);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupTopNavigation() {
        // Initialize category tabs
        tabAll = findViewById(R.id.tab_all);
        tabRacket = findViewById(R.id.tab_racket);
        tabShuttlecock = findViewById(R.id.tab_shuttlecock);
        tabFootwear = findViewById(R.id.tab_footwear);
        profileCircle = findViewById(R.id.profile_circle);
        profileCircle = findViewById(R.id.profile_circle);

        // Set click listeners for tabs
// Set click listeners for tabs
        View.OnClickListener tabClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset all tabs to dark gray rounded background (unselected state)
                tabAll.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
                tabRacket.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
                tabShuttlecock.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
                tabFootwear.setBackground(getResources().getDrawable(R.drawable.rounded_dark));

                tabAll.setTextColor(getResources().getColor(android.R.color.white));
                tabRacket.setTextColor(getResources().getColor(android.R.color.white));
                tabShuttlecock.setTextColor(getResources().getColor(android.R.color.white));
                tabFootwear.setTextColor(getResources().getColor(android.R.color.white));

                // Set the selected tab to green rounded background
                v.setBackground(getResources().getDrawable(R.drawable.rounded_green));
                ((TextView)v).setTextColor(getResources().getColor(android.R.color.white));

                // Update current category and refresh content
                if (v.getId() == R.id.tab_all) {
                    currentCategory = "all";
                } else if (v.getId() == R.id.tab_racket) {
                    currentCategory = "Racket";
                } else if (v.getId() == R.id.tab_shuttlecock) {
                    currentCategory = "Shuttlecock";
                } else if (v.getId() == R.id.tab_footwear) {
                    currentCategory = "Footwear";
                }

                // Refresh content based on selected category
                loadHomeFragment();
            }
        };

        // Set click listeners
        tabAll.setOnClickListener(tabClickListener);
        tabRacket.setOnClickListener(tabClickListener);
        tabShuttlecock.setOnClickListener(tabClickListener);
        tabFootwear.setOnClickListener(tabClickListener);

        // Set profile circle click listener
        profileCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load profile fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }
        });
    }
    // Add this method to the MainActivity class to allow the SearchFragment to change the category
    public void setCurrentCategory(String category) {
        this.currentCategory = category;

        // Update the UI to reflect the category change
        // Reset all tabs to dark gray rounded background (unselected state)
        tabAll.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
        tabRacket.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
        tabShuttlecock.setBackground(getResources().getDrawable(R.drawable.rounded_dark));
        tabFootwear.setBackground(getResources().getDrawable(R.drawable.rounded_dark));

        tabAll.setTextColor(getResources().getColor(android.R.color.white));
        tabRacket.setTextColor(getResources().getColor(android.R.color.white));
        tabShuttlecock.setTextColor(getResources().getColor(android.R.color.white));
        tabFootwear.setTextColor(getResources().getColor(android.R.color.white));

        // Set the selected tab to green rounded background
        if (category.equals("all")) {
            tabAll.setBackground(getResources().getDrawable(R.drawable.rounded_green));
            tabAll.setTextColor(getResources().getColor(android.R.color.white));
        } else if (category.equals("Racket")) {
            tabRacket.setBackground(getResources().getDrawable(R.drawable.rounded_green));
            tabRacket.setTextColor(getResources().getColor(android.R.color.white));
        } else if (category.equals("Shuttlecock")) {
            tabShuttlecock.setBackground(getResources().getDrawable(R.drawable.rounded_green));
            tabShuttlecock.setTextColor(getResources().getColor(android.R.color.white));
        } else if (category.equals("Footwear")) {
            tabFootwear.setBackground(getResources().getDrawable(R.drawable.rounded_green));
            tabFootwear.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void setupMainNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Home item now integrates the buy functionality in a HomeFragment
                    loadHomeFragment();
                    return true;
                } else if (item.getItemId() == R.id.nav_search) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SearchFragment())
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Load profile fragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Set default fragment to HomeFragment (which now includes buy functionality)
        loadHomeFragment();
    }

    public void loadHomeFragment() {
        // Set the current state to MAIN since we're using fragments
        currentState = ViewState.MAIN;

        // Create a new HomeFragment
        HomeFragment homeFragment = new HomeFragment();

        // Pass the current category to the fragment
        Bundle args = new Bundle();
        args.putString("category", currentCategory);
        homeFragment.setArguments(args);

        // Set a listener to handle when an item is clicked in the HomeFragment
        homeFragment.setOnItemSelectedListener(new HomeFragment.OnItemSelectedListener() {
            @Override
            public void onItemSelected(ItemModel item) {
                // Show detail page when an item is clicked
                showDetailPage(item.getId(), item.getName(), item.getImage(), item.getDescription(), item.getCategory(), item.getPrice());
            }
        });

        // Replace the current fragment with HomeFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    // Method to show the detail page using DetailFragment
    public void showDetailPage(long id, String name, int image, String description, String category, double price) {
        currentState = ViewState.DETAIL;

        // Save item information for later use (for the buy flow)
        itemId = id;
        itemName = name;
        itemImage = image;
        itemDescription = description;
        itemCategory = category;
        itemPrice = price;

        // Create and configure the DetailFragment
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", name);
        args.putInt("image", image);
        args.putString("description", description);
        args.putString("category", category);
        args.putDouble("price", price);
        detailFragment.setArguments(args);

        // Replace current fragment with DetailFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null) // Enables back navigation
                .commit();
    }

    // Method to show the buy page (formerly BuyPage)
    public void showBuyPage() {
        currentState = ViewState.CHECKOUT;
        setContentView(R.layout.activity_buy_page);

        // Initialize views
        cancelButton = findViewById(R.id.cancelButton);
        confirmButton = findViewById(R.id.confirmButton);
        itemNameTextView = findViewById(R.id.itemNameTextView);
        itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);
        itemCategoryTextView = findViewById(R.id.categoryTextView);
        itemPriceTextView = findViewById(R.id.priceTextView);
        buyPageImageView = findViewById(R.id.buyPageImageView);
        invoiceScrollView = findViewById(R.id.invoiceScrollView);
        invoiceTextView = findViewById(R.id.invoiceTextView);

        // Set data to views
        itemNameTextView.setText(itemName);
        itemDescriptionTextView.setText(itemDescription);
        itemCategoryTextView.setText("Category: " + itemCategory);

        // Format price as currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        itemPriceTextView.setText("Price: " + currencyFormat.format(itemPrice));

        buyPageImageView.setImageResource(itemImage);

        // Initially hide invoice
        if (invoiceScrollView != null) {
            invoiceScrollView.setVisibility(View.GONE);
        }

        // Set up button click listeners
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to detail page
                showDetailPage(itemId, itemName, itemImage, itemDescription, itemCategory, itemPrice);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Generate and display invoice
                generateInvoice();

                // Change button text
                confirmButton.setText("Return to Home");
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Return to main page
                        returnToMainLayout();
                    }
                });

                // Hide cancel button
                cancelButton.setVisibility(View.GONE);
            }
        });

        // Set window insets for the buy page
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void generateInvoice() {
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
    }

    // Method to return to the main layout
    public void returnToMainLayout() {
        currentState = ViewState.MAIN;
        setContentView(R.layout.activity_main);

        // Reinitialize necessary components
        setupMainNavigation();
        setupTopNavigation();

        // Update tab appearance to match current category
        updateTabsForCurrentCategory();
    }

    private void updateTabsForCurrentCategory() {
        // Reset all tabs
        tabAll.setBackgroundResource(android.R.color.transparent);
        tabRacket.setBackgroundResource(android.R.color.transparent);
        tabShuttlecock.setBackgroundResource(android.R.color.transparent);
        tabFootwear.setBackgroundResource(android.R.color.transparent);
        tabAll.setTextColor(getResources().getColor(android.R.color.white));
        tabRacket.setTextColor(getResources().getColor(android.R.color.white));
        tabShuttlecock.setTextColor(getResources().getColor(android.R.color.white));
        tabFootwear.setTextColor(getResources().getColor(android.R.color.white));

        // Set active tab based on current category
        if (currentCategory.equals("all")) {
            tabAll.setBackgroundResource(android.R.color.holo_blue_light);
            tabAll.setTextColor(getResources().getColor(android.R.color.black));
        } else if (currentCategory.equals("Racket")) {
            tabRacket.setBackgroundResource(android.R.color.holo_blue_light);
            tabRacket.setTextColor(getResources().getColor(android.R.color.black));
        } else if (currentCategory.equals("Shuttlecock")) {
            tabShuttlecock.setBackgroundResource(android.R.color.holo_blue_light);
            tabShuttlecock.setTextColor(getResources().getColor(android.R.color.black));
        } else if (currentCategory.equals("Footwear")) {
            tabFootwear.setBackgroundResource(android.R.color.holo_blue_light);
            tabFootwear.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    // Method to show detail page from search results
    public void showDetailPageFromSearch(ItemModel item) {
        // Use the existing method
        showDetailPage(item.getId(), item.getName(), item.getImage(), item.getDescription(), item.getCategory(), item.getPrice());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (dbManager != null) {
            dbManager.close();
        }
    }

    // Handle back button
    @Override
    public void onBackPressed() {
        switch (currentState) {
            case DETAIL:
                // Use the fragment manager's back stack
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    currentState = ViewState.MAIN;
                } else {
                    returnToMainLayout();
                }
                break;
            case CHECKOUT:
                showDetailPage(itemId, itemName, itemImage, itemDescription, itemCategory, itemPrice);
                break;
            case MAIN:
                super.onBackPressed();
                break;
        }
    }
}