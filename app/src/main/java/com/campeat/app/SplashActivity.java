package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // --- 1. AMBIL BAHASA HP ---
        String phoneLang = Locale.getDefault().getLanguage();
        // Contoh output: "id", "en", "ja", "ko", "ar"

        // --- 2. SETUP ANIMASI ---
        ImageView logoImage = findViewById(R.id.logoImage);
        LinearLayout titleRow = findViewById(R.id.titleRow);

        Animation topAnimation = AnimationUtils.loadAnimation(this, R.anim.move_from_top);
        Animation bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.move_from_bottom);

        logoImage.startAnimation(topAnimation);
        titleRow.startAnimation(bottomAnimation);

        // --- 3. SPLASH DELAY + KIRIM BAHASA HP ---
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, LanguageActivity.class);

                intent.putExtra("phone_lang", phoneLang);

                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
