package com.example.badmintonapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView searchResultsRecyclerView;
    private AdminDBManager dbManager;
    private CustomAdapter searchResultsAdapter;
    private List<ItemModel> searchResults;

    // Category views
    private LinearLayout racketCategory;
    private LinearLayout shuttlecockCategory;
    private LinearLayout footwearCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize UI components
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);

        // Initialize category components
        racketCategory = view.findViewById(R.id.racket_category);
        shuttlecockCategory = view.findViewById(R.id.shuttlecock_category);
        footwearCategory = view.findViewById(R.id.footwear_category);

        // Initialize database manager
        dbManager = new AdminDBManager(getContext());
        dbManager.open();

        // Set up RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults = new ArrayList<>();

        // Set up adapter with click listener
        searchResultsAdapter = new CustomAdapter(searchResults, new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemModel item) {
                // Navigate to detail page with the selected item
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showDetailPageFromSearch(item);
                }
            }
        });

        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Set click listener for search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get search query
                String query = searchEditText.getText().toString().trim();

                if (!query.isEmpty()) {
                    // Perform search operation
                    performSearch(query);
                } else {
                    Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up category click listeners
        setupCategoryListeners();

        return view;
    }

    private void setupCategoryListeners() {
        racketCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    // Change category in MainActivity and load the appropriate items
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.setCurrentCategory("Racket");
                    mainActivity.loadHomeFragment();
                }
            }
        });

        shuttlecockCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    // Change category in MainActivity and load the appropriate items
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.setCurrentCategory("Shuttlecock");
                    mainActivity.loadHomeFragment();
                }
            }
        });

        footwearCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    // Change category in MainActivity and load the appropriate items
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.setCurrentCategory("Footwear");
                    mainActivity.loadHomeFragment();
                }
            }
        });
    }

    private void performSearch(String query) {
        // Clear previous search results
        searchResults.clear();

        try {
            // Search items using the database manager
            Cursor cursor = dbManager.searchItems(query);

            if (cursor != null && cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    // Get data from cursor
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(AdminDatabaseHelper._ID));
                    String subject = cursor.getString(cursor.getColumnIndexOrThrow(AdminDatabaseHelper.SUBJECT));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow(AdminDatabaseHelper.DESC));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(AdminDatabaseHelper.CATEGORY));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(AdminDatabaseHelper.PRICE));

                    // Choose image based on category
                    int imageResId;
                    if (category.equalsIgnoreCase("Shuttlecock")) {
                        imageResId = R.drawable.shuttlecock;
                    } else if (category.equalsIgnoreCase("Footwear")) {
                        imageResId = R.drawable.badminton_shoes;
                    } else {
                        imageResId = R.drawable.racket;
                    }

                    // Create ItemModel and add to results
                    ItemModel item = new ItemModel(id, subject, desc, category, price, imageResId);
                    searchResults.add(item);

                    cursor.moveToNext();
                }
                cursor.close();
            }

            if (searchResults.isEmpty()) {
                Toast.makeText(getContext(), "No results found for: " + query, Toast.LENGTH_SHORT).show();
            }

            // Notify adapter that data has changed
            searchResultsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error during search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close database connection when fragment is destroyed
        if (dbManager != null) {
            dbManager.close();
        }
    }
}