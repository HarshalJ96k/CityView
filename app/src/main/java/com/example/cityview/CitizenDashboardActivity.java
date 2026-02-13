package com.example.cityview;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cityview.Fragments.AiAssistantFragment;
import com.example.cityview.Fragments.HomeFragment;
import com.example.cityview.Fragments.MapFragment;
import com.example.cityview.Fragments.ReportFragment;
import com.example.cityview.urls.ApiUrls;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

public class CitizenDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;

    // A launcher to listen for results from the ProfileActivity
    private final ActivityResultLauncher<Intent> profileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // This block runs if the profile was changed.
                    // We refresh the nav header to show the new image/name.
                    updateNavHeader();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_dashboard);

        sessionManager = new SessionManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::handleDrawerSelection);
        bottomNavigationView.setOnItemSelectedListener(this::handleBottomNavSelection);

        updateNavHeader();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean handleDrawerSelection(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_profile) {
            // We LAUNCH the activity using our launcher, instead of just starting it.
            Intent intent = new Intent(this, ProfileActivity.class);
            profileResultLauncher.launch(intent);
        } else if (itemId == R.id.nav_notifications) {
            startActivity(new Intent(CitizenDashboardActivity.this, NotificationsActivity.class));
        }
        else if (itemId == R.id.nav_contact) {
            startActivity(new Intent(this, ContactSupportActivity.class));
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean handleBottomNavSelection(MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_report) {
            selectedFragment = new ReportFragment();
        } else if (itemId == R.id.nav_map) {
            selectedFragment = new MapFragment();
        } else if (itemId == R.id.nav_ai) {
            selectedFragment = new AiAssistantFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        return false;
    }

    public void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_name);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);
        CircleImageView navProfileImage = headerView.findViewById(R.id.nav_header_profile_image);

        if (sessionManager.isLoggedIn()) {
            navUsername.setText(sessionManager.getUserName());
            navEmail.setText(sessionManager.getUserEmail());

            // Construct the full image URL and load it with Glide
            String imagePath = sessionManager.getProfileImagePath();
            String fullImageUrl = imagePath.isEmpty() ? null : ApiUrls.getRootUrl() + imagePath;

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(navProfileImage);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

