package com.example.cityview.activities;
import com.example.cityview.R;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.urls.ApiUrls;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextVerificationCode,
            editTextPhone, editTextAddress;
    private RadioGroup radioGroupUserType;
    private RadioButton radioButtonCitizen, radioButtonOfficial;
    private TextInputLayout textInputLayoutVerificationCode;
    private Button buttonSignUp;
    private TextView textViewSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextVerificationCode = findViewById(R.id.editTextVerificationCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        radioButtonCitizen = findViewById(R.id.radioButtonCitizen);
        radioButtonOfficial = findViewById(R.id.radioButtonOfficial);
        textInputLayoutVerificationCode = findViewById(R.id.textInputLayoutVerificationCode);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewSignIn = findViewById(R.id.textViewSignIn);

        radioGroupUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonOfficial) {
                    textInputLayoutVerificationCode.setVisibility(View.VISIBLE);
                } else {
                    textInputLayoutVerificationCode.setVisibility(View.GONE);
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish this activity and go back to Login
                finish();
            }
        });
    }

    private void registerUser() {
        final String fullName = editTextFullName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String address = editTextAddress.getText().toString().trim();
        final String verificationCode = editTextVerificationCode.getText().toString().trim();

        int selectedId = radioGroupUserType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        final String userType = selectedRadioButton.getText().toString();

        // --- Form Validation ---
        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters long");
            editTextPassword.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Address is required");
            editTextAddress.requestFocus();
            return;
        }

        if (userType.equals("Official") && verificationCode.isEmpty()) {
            editTextVerificationCode.setError("Verification code is required for officials");
            editTextVerificationCode.requestFocus();
            return;
        }

        // --- API Call ---
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiUrls.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();

                            if (status.equals("success")) {
                                // On successful registration, redirect to LoginActivity
                                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                // Clear the back stack so the user can't navigate back to registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegistrationActivity.this, "JSON Parsing Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegistrationActivity.this, "Registration Error: " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("phone_number", phone);
                params.put("address", address);
                params.put("user_type", userType);
                if (userType.equals("Official")) {
                    params.put("verification_code", verificationCode);
                }
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}


