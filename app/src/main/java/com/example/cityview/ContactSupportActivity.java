package com.example.cityview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ContactSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        Toolbar toolbar = findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact & Support");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        LinearLayout layoutPhone = findViewById(R.id.layout_phone);
        LinearLayout layoutEmail = findViewById(R.id.layout_email);
        LinearLayout layoutWebsite = findViewById(R.id.layout_website);
        LinearLayout layoutAddress = findViewById(R.id.layout_address);

        // Handle Phone Call
        layoutPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+91 9322162103"));
            startActivity(intent);
        });

        // Handle Email
        layoutEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:prokmc29@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from CityView App");
            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ContactSupportActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Website
        layoutWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.gpmzr.ac.in/"));
            startActivity(intent);
        });

        // Handle Address on Map
        layoutAddress.setOnClickListener(v -> {
            // Use a geo URI to open the location in a map application
            String mapUri = "geo:0,0?q=Murtizapur+Municipal+Corporation,+Shivaji+Chowk,+Murtizapur";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
            intent.setPackage("com.google.android.apps.maps"); // Attempt to open in Google Maps directly
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If Google Maps is not installed, open in any map app
                Intent genericMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
                if (genericMapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(genericMapIntent);
                } else {
                    Toast.makeText(this, "No map application found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity
        }
        return super.onOptionsItemSelected(item);
    }
}
