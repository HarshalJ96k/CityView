package com.example.cityview.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.cityview.LoginActivity;
import com.example.cityview.R;
import com.example.cityview.SessionManager;
import com.example.cityview.UpdateAdminProfileActivity;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminProfileFragment extends Fragment {

    private TextView txtName, txtEmail, txtPhone, txtCity;
    private ImageView imgProfile, btnEdit;
    private Button btnLogout;
    private SessionManager session;
    private String currentName = "", currentPhone = "", currentCity = "", currentImageUrl = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        txtName = v.findViewById(R.id.txt_name);
        txtEmail = v.findViewById(R.id.txt_email);
        txtPhone = v.findViewById(R.id.txt_phone);
        txtCity = v.findViewById(R.id.txt_city);
        imgProfile = v.findViewById(R.id.img_profile);
        btnEdit = v.findViewById(R.id.btn_edit_profile);
        btnLogout = v.findViewById(R.id.btn_logout);

        session = new SessionManager(requireContext());

        btnEdit.setOnClickListener(vw -> {
            Intent intent = new Intent(getContext(), UpdateAdminProfileActivity.class);
            intent.putExtra("fullName", currentName);
            intent.putExtra("phone", currentPhone);
            intent.putExtra("city", currentCity);
            intent.putExtra("imageUrl", currentImageUrl);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(vw -> {
            android.app.Dialog dialog = new android.app.Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_logout_confirm);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            android.widget.Button btnCancel = dialog.findViewById(R.id.btn_cancel_logout);
            android.widget.Button btnConfirm = dialog.findViewById(R.id.btn_confirm_logout);
            
            btnCancel.setOnClickListener(view -> dialog.dismiss());
            btnConfirm.setOnClickListener(view -> {
                session.clearSession();
                Intent i = new Intent(getContext(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                dialog.dismiss();
            });
            
            dialog.show();
        });

        loadProfile();
        return v;
    }

    private void loadProfile() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_ADMIN_PROFILE,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (!obj.getString("status").equals("success"))
                            return;

                        JSONObject d = obj.getJSONObject("data");

                        currentName = d.optString("full_name", "");
                        currentPhone = d.optString("phone", "");
                        currentCity = d.optString("city", "");
                        String imgPath = d.optString("profile_image_path", "");
                        currentImageUrl = imgPath.isEmpty() ? "" : ApiUrls.getRootUrl() + imgPath;

                        txtName.setText(currentName);
                        txtEmail.setText(d.optString("email"));
                        txtPhone.setText(currentPhone.isEmpty() ? "Not available" : currentPhone);
                        txtCity.setText(currentCity.isEmpty() ? "Not available" : currentCity);
                        if (!imgPath.isEmpty()) {
                            Glide.with(this)
                                    .load(ApiUrls.getRootUrl() + imgPath)
                                    .placeholder(R.drawable.ic_admin)
                                    .into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_admin);
                        }

                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", session.getUserId());
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }
}
