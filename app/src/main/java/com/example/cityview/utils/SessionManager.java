package com.example.cityview.utils;
import com.example.cityview.R;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private static final String PREF_NAME = "CityViewPref";
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_USER_ID = "userId";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_ROLE = "role";
    public static final String KEY_PROFILE_IMAGE_PATH = "profileImagePath";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /* 🔹 OLD METHOD (KEEP — used elsewhere) */
    public void createLoginSession(
            String id,
            String name,
            String email,
            String profileImagePath
    ) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PROFILE_IMAGE_PATH, profileImagePath);
        editor.apply();
    }

    /* 🔹 NEW METHOD (ROLE AWARE) */
    public void createLoginSession(
            String id,
            String name,
            String email,
            String phone,
            String role,
            String profileImagePath
    ) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_PROFILE_IMAGE_PATH, profileImagePath);
        editor.apply();
    }

    /* ================= GETTERS ================= */

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserPhone() {
        return pref.getString(KEY_PHONE, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "Citizen");
    }

    public String getProfileImagePath() {
        return pref.getString(KEY_PROFILE_IMAGE_PATH, "");
    }

    public void updateUserName(String name) {
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public void updateProfile(String name, String phone) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    public void updateProfileImagePath(String path) {
        editor.putString(KEY_PROFILE_IMAGE_PATH, path);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}


