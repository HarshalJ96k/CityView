package com.example.cityview.Fragments;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.cityview.models.Highlight;
import com.example.cityview.adapters.HighlightAdapter;
import com.example.cityview.R;
import com.example.cityview.urls.ApiUrls;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    /* ---------------- WEATHER UI ---------------- */
    private TextView textViewCityName, textViewDateTime, textViewTemperature,
            textViewCondition, textViewWind, textViewHumidity, textViewRain;
    private ImageView imageViewWeatherIcon;
    private ProgressBar progressBar;
    private CardView weatherCardView;

    /* ---------------- LOCATION ---------------- */
    private FusedLocationProviderClient fusedLocationClient;

    /* ---------------- CITY HIGHLIGHTS ---------------- */
    private RecyclerView highlightsRecyclerView;
    private HighlightAdapter highlightAdapter;
    private final List<Highlight> highlightList = new ArrayList<>();

    /* ---------------- PERMISSION ---------------- */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    getCurrentLocationAndData();
                } else {
                    Toast.makeText(getContext(),
                            "Location permission required", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        /* -------- WEATHER VIEWS -------- */
        textViewCityName = view.findViewById(R.id.text_city_name);
        textViewDateTime = view.findViewById(R.id.text_date_time);
        textViewTemperature = view.findViewById(R.id.text_temperature);
        textViewCondition = view.findViewById(R.id.text_weather_condition);
        textViewWind = view.findViewById(R.id.text_wind_speed);
        textViewHumidity = view.findViewById(R.id.text_humidity);
        textViewRain = view.findViewById(R.id.text_rain_chance);
        imageViewWeatherIcon = view.findViewById(R.id.image_view_weather_icon);
        progressBar = view.findViewById(R.id.progress_bar_weather);
        weatherCardView = view.findViewById(R.id.weather_card_view);

        /* -------- CITY HIGHLIGHTS -------- */
        highlightsRecyclerView = view.findViewById(R.id.recycler_view_highlights);
        highlightAdapter = new HighlightAdapter(getContext(), highlightList);

        int columns = calculateNoOfColumns(180); // 180dp per card
        highlightsRecyclerView.setLayoutManager(
                new GridLayoutManager(getContext(), columns));


        highlightsRecyclerView.setAdapter(highlightAdapter);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        updateDateTime();
        getCurrentLocationAndData();

        return view;
    }

    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp =
                getResources().getDisplayMetrics().widthPixels /
                        getResources().getDisplayMetrics().density;

        return Math.max(2, (int) (screenWidthDp / columnWidthDp));
    }

    /* ---------------- DATE ---------------- */
    private void updateDateTime() {
        String date = new SimpleDateFormat(
                "EEEE, MMMM d, yyyy", Locale.getDefault()).format(new Date());
        textViewDateTime.setText(date);
    }

    /* ---------------- LOCATION ---------------- */
    @android.annotation.SuppressLint("MissingPermission")
    private void getCurrentLocationAndData() {

        progressBar.setVisibility(View.VISIBLE);
        weatherCardView.setVisibility(View.INVISIBLE);

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (!isAdded()) return;

                        if (location != null) {
                            fetchWeatherData(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                        } else {
                            // 🔥 FALLBACK CITY
                            String fallbackCity = "Murtizapur";
                            textViewCityName.setText(fallbackCity);
                            progressBar.setVisibility(View.GONE);

                            loadCityHighlightsUnsplash(fallbackCity);
                        }
                    });

        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /* ---------------- WEATHER API ---------------- */
    private void fetchWeatherData(double lat, double lon) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_WEATHER,
                response -> {

                    progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getString("status").equals("success")) return;

                        JSONObject data = json.getJSONObject("data");
                        String cityName = data.getString("city");

                        textViewCityName.setText(cityName);
                        textViewTemperature.setText(data.getInt("temperature") + "°");
                        textViewCondition.setText(data.getString("condition"));
                        textViewWind.setText(data.getInt("wind_speed") + " km/h");
                        textViewHumidity.setText(data.getInt("humidity") + "%");
                        textViewRain.setText(data.getDouble("rain") + " mm");

                        String iconUrl =
                                "https://openweathermap.org/img/wn/"
                                        + data.getString("icon")
                                        + "@4x.png";

                        Glide.with(this)
                                .load(iconUrl)
                                .into(imageViewWeatherIcon);

                        weatherCardView.setVisibility(View.VISIBLE);

                        // ✅ LOAD CITY HIGHLIGHTS (UNSPLASH)
                        loadCityHighlightsUnsplash(cityName);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> progressBar.setVisibility(View.GONE)) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("lat", String.valueOf(lat));
                map.put("lon", String.valueOf(lon));
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /* ---------------- CITY HIGHLIGHTS (UNSPLASH) ---------------- */
    private void loadCityHighlightsUnsplash(String city) {

        String url = "https://api.unsplash.com/search/photos"
                + "?query=" + city
                + "&per_page=5"
                + "&orientation=landscape"
                + "&client_id=wk6AWFhXLXJmuQF0v7fTtQEG8nRUbE1htZoiTon3C8s";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray results = json.getJSONArray("results");

                        highlightList.clear();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            String imageUrl = obj
                                    .getJSONObject("urls")
                                    .getString("regular");

                            highlightList.add(
                                    new Highlight(city + " View", imageUrl)
                            );
                        }

                        highlightAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(
                        getContext(),
                        "City highlights unavailable",
                        Toast.LENGTH_SHORT
                ).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}

