package com.example.badmintonapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailFragment extends Fragment {
    private static final String TAG = "DetailFragment";
    private Button goBackButton;
    private Button buyButton;
    private String itemName;
    private int itemImage;
    private String itemDescription;
    private String itemCategory;
    private double itemPrice;
    private long itemId;
    private AdminDBManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Initialize buttons
        goBackButton = view.findViewById(R.id.goBack);
        buyButton = view.findViewById(R.id.buyButton);

        // Initialize UI elements
        TextView nameTextView = view.findViewById(R.id.detail_name);
        ImageView imageView = view.findViewById(R.id.detail_image);
        TextView descriptionTextView = view.findViewById(R.id.detail_description);
        TextView categoryTextView = view.findViewById(R.id.detail_category);
        TextView priceTextView = view.findViewById(R.id.detail_price);

        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            itemId = args.getLong("id", -1);
            itemName = args.getString("name", "");
            itemImage = args.getInt("image", R.drawable.racket);
            itemDescription = args.getString("description", "");
            itemCategory = args.getString("category", "Racket");
            itemPrice = args.getDouble("price", 0.0);

            // Load item details from database if ID is provided
            if (itemId != -1) {
                loadItemDetails(itemId);
            }

            // Update UI with item information
            nameTextView.setText(itemName);
            imageView.setImageResource(itemImage);
            descriptionTextView.setText(itemDescription);
            categoryTextView.setText("Category: " + itemCategory);

            // Format the price as currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            priceTextView.setText("Price: " + currencyFormat.format(itemPrice));

        } else {
            Log.e(TAG, "No arguments provided to DetailFragment");
            Toast.makeText(getContext(), "Error: Item details not available", Toast.LENGTH_SHORT).show();
        }

        // Set up "Go Back" button
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous fragment
                getParentFragmentManager().popBackStack();
            }
        });

        // Set up "Buy Now" button
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Check if user is logged in
                    SharedPreferences sp = getActivity().getSharedPreferences("UserSession",
                            Context.MODE_PRIVATE);
                    boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);

                    if (!isLoggedIn) {
                        // If not logged in, redirect to RegisterActivity
                        Toast.makeText(getActivity(), "Please log in or register to purchase items",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getActivity(), RegisterActivity.class));
                    } else {
                        // If logged in, launch BuyPage activity with all item details
                        Intent intent = new Intent(getActivity(), BuyPage.class);
                        intent.putExtra("itemName", itemName);
                        intent.putExtra("itemDescription", itemDescription);
                        intent.putExtra("itemCategory", itemCategory);
                        intent.putExtra("itemPrice", itemPrice);
                        intent.putExtra("itemImage", itemImage);
                        intent.putExtra("itemId", itemId);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error launching buy page: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error: Cannot proceed to checkout",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Load item details from database
    private void loadItemDetails(long itemId) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot load item details");
            return;
        }

        try {
            dbManager = new AdminDBManager(getContext());
            dbManager.open();

            // Get item details from database
            Cursor cursor = dbManager.getItemById(itemId);

            if (cursor != null && cursor.moveToFirst()) {
                int subjectIndex = cursor.getColumnIndex(AdminDatabaseHelper.SUBJECT);
                int descIndex = cursor.getColumnIndex(AdminDatabaseHelper.DESC);
                int categoryIndex = cursor.getColumnIndex(AdminDatabaseHelper.CATEGORY);
                int priceIndex = cursor.getColumnIndex(AdminDatabaseHelper.PRICE);

                if (subjectIndex >= 0) {
                    itemName = cursor.getString(subjectIndex);
                }

                if (descIndex >= 0) {
                    itemDescription = cursor.getString(descIndex);
                }

                if (categoryIndex >= 0 && !cursor.isNull(categoryIndex)) {
                    itemCategory = cursor.getString(categoryIndex);

                    // Set image based on category
                    if (itemCategory.equalsIgnoreCase("Shuttlecock")) {
                        itemImage = R.drawable.shuttlecock;
                    } else if (itemCategory.equalsIgnoreCase("Footwear")) {
                        itemImage = R.drawable.badminton_shoes;
                    } else {
                        itemImage = R.drawable.racket;
                    }
                }

                if (priceIndex >= 0) {
                    itemPrice = cursor.getDouble(priceIndex);
                }

                cursor.close();
            } else {
                Log.e(TAG, "Item not found or cursor is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading item details: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}