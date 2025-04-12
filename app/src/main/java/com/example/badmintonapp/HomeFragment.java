package com.example.badmintonapp;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private List<ItemModel> itemList;
    private AdminDBManager dbManager;
    private OnItemSelectedListener listener;
    private String currentCategory = "all"; // Default to showing all items

    // Carousel components
    private ViewPager2 imageCarousel;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private int[] carouselImages = {R.drawable.shoes, R.drawable.racket2, R.drawable.shuttlecock2};

    // Interface for item click callback
    public interface OnItemSelectedListener {
        void onItemSelected(ItemModel item);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get category from arguments if available
        Bundle args = getArguments();
        if (args != null && args.containsKey("category")) {
            currentCategory = args.getString("category", "all");
        }

        // Setup image carousel
        setupCarousel(view);

        // Setup recycler view for products
        recyclerView = view.findViewById(R.id.recyclerview);
        setupRecyclerView();

        return view;
    }

    private void setupCarousel(View view) {
        try {
            // Initialize the ViewPager2
            imageCarousel = view.findViewById(R.id.image_carousel);

            // Set up the adapter with just the images
            CarouselAdapter carouselAdapter = new CarouselAdapter(carouselImages);
            imageCarousel.setAdapter(carouselAdapter);

            // Set up auto-scrolling
            autoScrollHandler = new Handler();
            autoScrollRunnable = new Runnable() {
                @Override
                public void run() {
                    if (imageCarousel != null) {
                        int nextItem = (imageCarousel.getCurrentItem() + 1) % carouselImages.length;
                        imageCarousel.setCurrentItem(nextItem, true);
                        autoScrollHandler.postDelayed(this, 20000); // Auto-scroll every 1.5 seconds
                    }
                }
            };

            // Start auto-scrolling
            autoScrollHandler.postDelayed(autoScrollRunnable, 20000);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up carousel: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        try {
            // Use GridLayoutManager with 2 columns instead of LinearLayoutManager
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

            // Initialize the item list
            itemList = new ArrayList<>();

            // Initialize adapter with click listener
            adapter = new CustomAdapter(itemList, new CustomAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(ItemModel item) {
                    if (listener != null) {
                        listener.onItemSelected(item);
                    }
                }
            });

            recyclerView.setAdapter(adapter);

            // Load items based on current category
            loadItems();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItems() {
        try {
            // Initialize database manager
            dbManager = new AdminDBManager(getContext());
            dbManager.open();

            // Clear existing items
            itemList.clear();

            // Get cursor based on category (null or "all" means get all items)
            Cursor cursor;
            if (currentCategory == null || currentCategory.equalsIgnoreCase("all")) {
                cursor = dbManager.fetch();
            } else {
                cursor = dbManager.fetchByCategory(currentCategory);
            }

            // Process cursor and add items to the list
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

                    // Add to list with all properties including price
                    itemList.add(new ItemModel(id, subject, desc, category, price, imageResId));

                    cursor.moveToNext();
                }
                cursor.close();
            }

            // No items to display (empty database and no demo items)
            if (itemList.isEmpty()) {
                Toast.makeText(getContext(), "No items available in this category", Toast.LENGTH_SHORT).show();
            }

            // Notify adapter that data has changed
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Error loading items: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop auto-scrolling when fragment is paused
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume auto-scrolling when fragment is resumed
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 1500);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (dbManager != null) {
            dbManager.close();
        }

        // Make sure to remove callbacks to prevent memory leaks
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
}