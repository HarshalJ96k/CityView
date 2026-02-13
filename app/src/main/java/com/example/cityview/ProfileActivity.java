package com.example.cityview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView headerProfileName, headerProfileEmail, textFullName, textEmail, textPhone, textAddress;
    private CircleImageView profileImageView;
    private ImageView imageEditProfile;

    private String currentPhone, currentAddress, currentImageUrl;

    // Use the modern ActivityResultLauncher to get the result from EditProfileActivity
    private final ActivityResultLauncher<Intent> editProfileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // This block runs if the user saved changes in EditProfileActivity.
                    // We refresh the profile data to show the latest updates.
                    fetchUserDetails();
                    // We also set our own result to OK, so the Dashboard can update the nav header if needed.
                    setResult(RESULT_OK);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);

        // Initialize all Views from the new layout
        headerProfileName = findViewById(R.id.profile_name);
        headerProfileEmail = findViewById(R.id.profile_email);
        textFullName = findViewById(R.id.text_full_name);
        textEmail = findViewById(R.id.text_email);
        textPhone = findViewById(R.id.text_phone);
        textAddress = findViewById(R.id.text_address);
        profileImageView = findViewById(R.id.profile_image);
        imageEditProfile = findViewById(R.id.imageEditProfile);

        // Set an OnClickListener for the edit icon
        imageEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // Pass the currently displayed data to the edit screen so it can pre-fill the fields
            intent.putExtra("fullName", headerProfileName.getText().toString());
            intent.putExtra("phone", currentPhone);
            intent.putExtra("address", currentAddress);
            intent.putExtra("imageUrl", currentImageUrl);
            editProfileResultLauncher.launch(intent);
        });

        // Populate basic data from SessionManager immediately
        if (sessionManager.isLoggedIn()) {
            updateUIFromSession();
            // Fetch the rest of the details (phone, address, image) from the server
            fetchUserDetails();
        } else {
            // This is a safeguard, though the user shouldn't be here if not logged in.
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUIFromSession() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        headerProfileName.setText(name);
        headerProfileEmail.setText(email);
        textFullName.setText(name);
        textEmail.setText(email);
    }

    private void fetchUserDetails() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Error: User ID not found in session.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiUrls.URL_GET_USER_DETAILS,
                response -> {
                    Log.d("ProfileActivity", "Server Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject userObject = jsonObject.getJSONObject("user");

                            // Store the fetched details to pass to the Edit screen later
                            currentPhone = userObject.optString("phone_number", "Not available");
                            currentAddress = userObject.optString("address", "Not available");
                            String imagePath = userObject.optString("profile_image_path", "");

                            // Construct the full image URL
                            currentImageUrl = imagePath.isEmpty() ? null : ApiUrls.getRootUrl() + imagePath;

                            // Update the UI with the fetched data
                            textPhone.setText(currentPhone);
                            textAddress.setText(currentAddress);

                            Glide.with(this)
                                    .load(currentImageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder) // Show placeholder if image fails to load
                                    .into(profileImageView);

                        } else {
                            Toast.makeText(ProfileActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ProfileActivity.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(ProfileActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle the back arrow click in the toolbar
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

