package com.example.cityview.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.example.cityview.R;
import com.example.cityview.SessionManager;
import com.example.cityview.VolleyMultipartRequest;
import com.example.cityview.urls.ApiUrls;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {

    private ImageView imagePreview;
    private TextInputEditText edtDescription, edtLocation;
    private Spinner spinnerCategory;
    private Button btnSubmit, btnUseLocation;
    private View uploadArea;

    private Bitmap bitmap;
    private Uri photoUri;

    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationClient;

    /* ================= PERMISSION LAUNCHERS ================= */

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = true;
                for (Boolean value : result.values()) {
                    if (!value) {
                        granted = false;
                        break;
                    }
                }
                if (granted)
                    openCamera();
                else
                    Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(), photoUri);
                        imagePreview.setImageBitmap(bitmap);
                        imagePreview.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    /* ================= FRAGMENT ================= */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        imagePreview = view.findViewById(R.id.image_preview);
        edtDescription = view.findViewById(R.id.edit_text_description);
        edtLocation = view.findViewById(R.id.edit_text_location);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnSubmit = view.findViewById(R.id.button_submit_report);
        btnUseLocation = view.findViewById(R.id.button_use_current_location);
        uploadArea = view.findViewById(R.id.layout_upload_photo);

        sessionManager = new SessionManager(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupCategorySpinner();

        uploadArea.setOnClickListener(v -> checkPermissionsAndOpenCamera());
        btnUseLocation.setOnClickListener(v -> getCurrentLocation());
        btnSubmit.setOnClickListener(v -> submitReport());

        return view;
    }

    /* ================= CATEGORY SPINNER ================= */

    private void setupCategorySpinner() {
        String[] categories = {
                "General",
                "Road & Footpath",
                "Water Supply",
                "Electricity",
                "Garbage & Sanitation",
                "Drainage & Sewage",
                "Public Property",
                "Street Lighting",
                "Parks & Gardens",
                "Noise Pollution",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        // Default is index 0 = "General" automatically
    }

    /* ================= CAMERA PERMISSION (FIXED) ================= */

    private void checkPermissionsAndOpenCamera() {

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                permissionLauncher.launch(new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                });
            } else {
                openCamera();
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                permissionLauncher.launch(new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            } else {
                openCamera();
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.cityview.provider",
                    photoFile);
            cameraLauncher.launch(photoUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Camera error", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
    }

    /* ================= LOCATION ================= */

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    edtLocation.setText(
                            location.getLatitude() + ", " + location.getLongitude());
                } else {
                    Toast.makeText(getContext(), "Enable GPS", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 100);
        }
    }

    /* ================= SUBMIT REPORT ================= */

    private void submitReport() {

        String description = edtDescription.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String userId = sessionManager.getUserId();

        // Category is optional — default is "General" (index 0)
        String category = (spinnerCategory.getSelectedItem() != null)
                ? spinnerCategory.getSelectedItem().toString()
                : "General";

        if (description.isEmpty() || location.isEmpty() || bitmap == null) {
            Toast.makeText(getContext(), "Fill all fields & capture photo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, ApiUrls.URL_SUBMIT_REPORT,
                response -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Report");
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        Toast.makeText(getContext(),
                                obj.getString("message"), Toast.LENGTH_LONG).show();

                        if (obj.getString("status").equals("success")) {
                            edtDescription.setText("");
                            edtLocation.setText("");
                            imagePreview.setVisibility(View.GONE);
                            bitmap = null;
                            spinnerCategory.setSelection(0); // reset to "General"
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Report");
                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", userId);
                map.put("description", description);
                map.put("location", location);
                map.put("category", category); // ✅ NEW: send chosen category
                return map;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> map = new HashMap<>();
                map.put("image", new DataPart(
                        System.currentTimeMillis() + ".jpg",
                        getImageBytes(bitmap)));
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private byte[] getImageBytes(Bitmap bmp) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        return bos.toByteArray();
    }
}
