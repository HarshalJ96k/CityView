package com.example.cityview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.urls.ApiUrls;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);

        sessionManager = new SessionManager(this);

        /* ✅ FIXED AUTO LOGIN */
        if (sessionManager.isLoggedIn()) {

            String role = sessionManager.getUserRole();
            Intent intent;

            if (role.equalsIgnoreCase("Official")) {
                intent = new Intent(this, OfficialDashboardActivity.class);
            } else {
                intent = new Intent(this, CitizenDashboardActivity.class);
            }

            startActivity(intent);
            finish();
            return;
        }

        buttonSignIn.setOnClickListener(v -> loginUser());

        textViewSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void loginUser() {

        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_LOGIN,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (!json.getString("status").equals("success")) {
                            Toast.makeText(this,
                                    json.getString("message"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject user = json.getJSONObject("user");

                        String id = user.getString("id");
                        String name = user.getString("full_name");
                        String emailDb = user.getString("email");
                        String phone = user.optString("phone_number", "");
                        String role = user.getString("user_type");
                        String profileImagePath =
                                user.optString("profile_image_path", "");

                        /* ✅ STORE ROLE PROPERLY */
                        sessionManager.createLoginSession(
                                id,
                                name,
                                emailDb,
                                phone,
                                role,
                                profileImagePath
                        );

                        Intent intent;
                        if (role.equalsIgnoreCase("Official")) {
                            intent = new Intent(this, OfficialDashboardActivity.class);
                        } else {
                            intent = new Intent(this, CitizenDashboardActivity.class);
                        }

                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        Toast.makeText(this,
                                "Parsing error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Login failed",
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("password", password);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
