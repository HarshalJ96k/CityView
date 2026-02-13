package com.example.cityview;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cityview.Fragments.AdminProfileFragment;
import com.example.cityview.Fragments.AllUsersFragment;
import com.example.cityview.Fragments.CompletedReportsFragment;
import com.example.cityview.Fragments.InProgressReportsFragment;
import com.example.cityview.Fragments.OfficialDashboardFragment;
import com.example.cityview.Fragments.PendingReportsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OfficialDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_official);
        setSupportActionBar(toolbar);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_official);
        bottomNav.setOnItemSelectedListener(navListener);

        // Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_official,
                            new OfficialDashboardFragment())
                    .commit();
        }
    }

    /* 🔹 Inflate TOP MENU (All Users) */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.official_menu, menu);
        return true;
    }

    /* 🔹 Handle TOP MENU clicks */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.official_menu_all_user) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_official,
                            new AllUsersFragment())
                    .addToBackStack(null)
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 🔹 Bottom Navigation handling */
    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {

                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_official_dashboard) {
                    selectedFragment = new OfficialDashboardFragment();

                } else if (itemId == R.id.nav_official_pending) {
                    selectedFragment = new PendingReportsFragment();

                } else if (itemId == R.id.nav_official_progress) {
                    selectedFragment = new InProgressReportsFragment();

                } else if (itemId == R.id.nav_official_received) {
                    selectedFragment = new CompletedReportsFragment();

                } else if (itemId == R.id.nav_official_profile) {
                    selectedFragment = new AdminProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_official,
                                    selectedFragment)
                            .commit();
                }

                return true;
            };
}
