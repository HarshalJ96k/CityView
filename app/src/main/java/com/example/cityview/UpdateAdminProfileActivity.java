package com.example.cityview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.urls.ApiUrls;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdateAdminProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    EditText edtName, edtPhone, edtCity;
    ImageView imgProfile;
    TextView txtChangePhoto;
    Button btnUpdate;

    SessionManager session;
    Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin_profile);

        edtName = findViewById(R.id.edit_name);
        edtPhone = findViewById(R.id.edit_phone);
        edtCity = findViewById(R.id.edit_city);
        imgProfile = findViewById(R.id.img_profile);
        txtChangePhoto = findViewById(R.id.txt_change_photo);
        btnUpdate = findViewById(R.id.btn_update_profile);

        session = new SessionManager(this);

//        edtName.setText(session.getUserName());
//        edtPhone.setText(session.getUserPhone());

        txtChangePhoto.setOnClickListener(v -> openGallery());
        imgProfile.setOnClickListener(v -> openGallery());

        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                selectedBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                imgProfile.setImageBitmap(selectedBitmap);
            } catch (Exception ignored) {}
        }
    }

    private void updateProfile() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_UPDATE_ADMIN_PROFILE,
                response -> {
                    // 🔥 UPDATE SESSION LOCALLY
                    session.updateProfile(
                            edtName.getText().toString(),
                            edtPhone.getText().toString()
                    );
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this,
                        "Update failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<>();
                map.put("id", session.getUserId());
                map.put("full_name", edtName.getText().toString());
                map.put("phone_number", edtPhone.getText().toString());
                map.put("address", edtCity.getText().toString());

                if (selectedBitmap != null) {
                    map.put("profile_image", bitmapToBase64(selectedBitmap));
                }
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String bitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(
                baos.toByteArray(), Base64.NO_WRAP);
    }
}
