package com.example.badmintonapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private TextView usernameTextView;
    private Button logoutButton;
    private Button loginButton;
    private Button registerButton;
    private Button adminPanelButton;
    private LinearLayout loggedInLayout;
    private LinearLayout loggedOutLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        usernameTextView = view.findViewById(R.id.username_text_view);
        logoutButton = view.findViewById(R.id.logout_button);
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.register_button);
        adminPanelButton = view.findViewById(R.id.admin_panel_button);
        loggedInLayout = view.findViewById(R.id.logged_in_layout);
        loggedOutLayout = view.findViewById(R.id.logged_out_layout);

        // Check login status
        SharedPreferences sp = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);
        boolean isAdmin = sp.getBoolean("isAdmin", false);

        if (isLoggedIn) {
            // Show logged in UI
            String username = sp.getString("username", "User");
            usernameTextView.setText("Welcome, " + username);
            loggedInLayout.setVisibility(View.VISIBLE);
            loggedOutLayout.setVisibility(View.GONE);

            // Show/hide admin panel button based on admin status
            if (isAdmin) {
                adminPanelButton.setVisibility(View.VISIBLE);
            } else {
                adminPanelButton.setVisibility(View.GONE);
            }
        } else {
            // Show logged out UI
            loggedInLayout.setVisibility(View.GONE);
            loggedOutLayout.setVisibility(View.VISIBLE);
        }

        // Set click listener for admin panel button
        adminPanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Admin.class);
                startActivity(intent);
            }
        });

        // Set click listener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear shared preferences
                SharedPreferences sp = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
                sp.edit().clear().apply();

                // Refresh the current activity to show logged out state
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
