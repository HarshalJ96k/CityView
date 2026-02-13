package com.example.cityview.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.ChatAdapter;
import com.example.cityview.Message;
import com.example.cityview.R;
import com.example.cityview.urls.ApiUrls;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AiAssistantFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private RequestQueue requestQueue;
    private LinearLayout suggestionsLayout;

    // Location components
    private FusedLocationProviderClient fusedLocationClient;
    private String currentUserCity = "Kolhapur"; // Default city

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentCity();
                } else {
                    Toast.makeText(getContext(), "Location permission denied. Using default city.", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_assistant, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_chat);
        editTextMessage = view.findViewById(R.id.edit_text_message);
        buttonSend = view.findViewById(R.id.button_send);
        suggestionsLayout = view.findViewById(R.id.suggestions_layout);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        requestQueue = Volley.newRequestQueue(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        getCurrentCity();
        addMessage("Hello! I'm your CityView AI assistant. How can I help you today?", false);

        buttonSend.setOnClickListener(v -> handleSendQuestion());

        // Setup suggestion buttons
        TextView suggestion1 = view.findViewById(R.id.suggestion1);
        TextView suggestion2 = view.findViewById(R.id.suggestion2);
        TextView suggestion3 = view.findViewById(R.id.suggestion3);
        TextView suggestion4 = view.findViewById(R.id.suggestion4);

        suggestion1.setOnClickListener(v -> handleSuggestionClick(suggestion1.getText().toString()));
        suggestion2.setOnClickListener(v -> handleSuggestionClick(suggestion2.getText().toString()));
        suggestion3.setOnClickListener(v -> handleSuggestionClick(suggestion3.getText().toString()));
        suggestion4.setOnClickListener(v -> handleSuggestionClick(suggestion4.getText().toString()));

        return view;
    }

    private void handleSendQuestion() {
        String question = editTextMessage.getText().toString().trim();
        if (!question.isEmpty()) {
            addMessage(question, true);
            getAIResponse(question);
            editTextMessage.setText("");
            // Hide suggestions after the first manual question
            if (suggestionsLayout.getVisibility() == View.VISIBLE) {
                suggestionsLayout.setVisibility(View.GONE);
            }
        }
    }

    private void handleSuggestionClick(String question) {
        addMessage(question, true);
        getAIResponse(question);
        // Hide suggestions after a suggestion is clicked
        if (suggestionsLayout.getVisibility() == View.VISIBLE) {
            suggestionsLayout.setVisibility(View.GONE);
        }
    }


    private void getCurrentCity() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    currentUserCity = addresses.get(0).getLocality();
                                    if(currentUserCity != null){
                                        Toast.makeText(getContext(), "Location set to: " + currentUserCity, Toast.LENGTH_SHORT).show();
                                    } else {
                                        currentUserCity = "Kolhapur"; // Fallback if locality is null
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getContext(), "Could not determine location. Using default.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void addMessage(String text, boolean isSentByUser) {
        messageList.add(new Message(text, isSentByUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
    private void getAIResponse(final String question) {

        addMessage("Typing...", false);
        final int typingIndex = messageList.size() - 1;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_AI_ASSISTANT,
                response -> {

                    messageList.remove(typingIndex);
                    chatAdapter.notifyItemRemoved(typingIndex);

                    try {
                        JSONObject obj = new JSONObject(response);
                        String answer = obj.optString("answer", "No response");

                        addMessage(answer, false);

                    } catch (Exception e) {
                        addMessage("⚠️ Invalid server response.", false);
                    }
                },
                error -> {
                    messageList.remove(typingIndex);
                    chatAdapter.notifyItemRemoved(typingIndex);
                    addMessage("Network error. Please try again.", false);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("question", question);
                params.put("city", currentUserCity);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

}

