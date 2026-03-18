package com.example.cityview.activities;
import com.example.cityview.R;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);

        // Initial state for animation
        ivLogo.setAlpha(0f);
        tvAppName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        tvAppName.setTranslationY(30f);
        tvTagline.setTranslationY(30f);

        // Setup animations
        ivLogo.animate().scaleX(1.1f).scaleY(1.1f).alpha(1f).setDuration(1200).start();
        tvAppName.animate().translationY(0f).alpha(1f).setDuration(1200).setStartDelay(200).start();
        tvTagline.animate().translationY(0f).alpha(1f).setDuration(1200).setStartDelay(400).start();

        // Delay to start next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3000); // 3 seconds total
    }
}


