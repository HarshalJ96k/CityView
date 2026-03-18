package com.example.cityview.Fragments;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.R;
import com.example.cityview.urls.ApiUrls;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    moveToCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestQueue = Volley.newRequestQueue(requireContext());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setBuildingsEnabled(true);

        checkLocationPermission();
    }

    /* ================= LOCATION + CAMERA ================= */

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void moveToCurrentLocation() {

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        googleMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(getContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
                return;
            }

            LatLng currentLatLng =
                    new LatLng(location.getLatitude(), location.getLongitude());

            // 🔥 3D CAMERA POSITION
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLatLng)
                    .zoom(17f)
                    .tilt(60f)      // 3D tilt
                    .bearing(0f)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            getCityFromLocation(location.getLatitude(), location.getLongitude());
        });
    }

    /* ================= CITY NAME ================= */

    private void getCityFromLocation(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
//                if (city != null) {
////                    fetchCityBoundary(city);
//                } else {
//                    Toast.makeText(getContext(), "City not detected", Toast.LENGTH_SHORT).show();
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    /* ================= CITY BOUNDARY ================= */
//
//    private void fetchCityBoundary(String city) {
//
//        String url = ApiUrls.URL_GET_CITY_BOUNDARY + "?city=" + city;
//
//        StringRequest request = new StringRequest(Request.Method.GET, url,
//                response -> {
//                    try {
//                        JSONObject obj = new JSONObject(response);
//                        if (obj.getString("status").equals("success")) {
//                            drawBoundary(obj.getJSONObject("geojson"));
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                error -> Toast.makeText(getContext(),
//                        "Boundary fetch failed", Toast.LENGTH_SHORT).show()
//        );
//
//        requestQueue.add(request);
//    }

    private void drawBoundary(JSONObject geojson) throws Exception {

        PolygonOptions polygonOptions = new PolygonOptions();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        List<LatLng> points = new ArrayList<>();

        String type = geojson.getString("type");

        if (type.equals("Polygon")) {
            JSONArray coords = geojson.getJSONArray("coordinates").getJSONArray(0);
            for (int i = 0; i < coords.length(); i++) {
                JSONArray p = coords.getJSONArray(i);
                LatLng latLng = new LatLng(p.getDouble(1), p.getDouble(0));
                points.add(latLng);
                boundsBuilder.include(latLng);
            }
        }

        polygonOptions.addAll(points);

        List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20));
        polygonOptions.strokePattern(pattern);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.strokeWidth(6f);
        polygonOptions.fillColor(Color.TRANSPARENT);

        googleMap.addPolygon(polygonOptions);

        if (!points.isEmpty()) {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
            );
        }
    }
}

