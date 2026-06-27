package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // --- 1. AMBIL BAHASA HP ---
        String phoneLang = Locale.getDefault().getLanguage();

        // --- 2. SETUP ANIMASI ---
        ImageView logoImage = findViewById(R.id.logoImage);
        LinearLayout titleRow = findViewById(R.id.titleRow);

        Animation topAnimation = AnimationUtils.loadAnimation(this, R.anim.move_from_top);
        Animation bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.move_from_bottom);

        logoImage.startAnimation(topAnimation);
        titleRow.startAnimation(bottomAnimation);

        // --- 3. SPLASH DELAY + KIRIM BAHASA HP ---
        new Handler().postDelayed(() -> {

            // CEK APAKAH SUDAH LOGIN
            com.google.firebase.auth.FirebaseUser currentUser =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // sudah login, cek role dulu
                FirebaseDatabase.getInstance(
                                ""
                        ).getReference("users")
                        .child(currentUser.getUid())
                        .child("role")
                        .get()
                        .addOnSuccessListener(snapshot -> {

                            String role = snapshot.getValue(String.class);
                            Intent intent;

                            if ("admin".equals(role)) {
                                intent = new Intent(SplashActivity.this, HomeAdminActivity.class);
                            } else {
                                intent = new Intent(SplashActivity.this, HomeActivity.class);
                            }

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // gagal cek role, arahkan ke language
                            startActivity(new Intent(SplashActivity.this, LanguageActivity.class));
                            finish();
                        });

            } else {
                // belum login, arahkan ke language seperti biasa
                Intent intent = new Intent(SplashActivity.this, LanguageActivity.class);
                intent.putExtra("phone_lang", Locale.getDefault().getLanguage());
                startActivity(intent);
                finish();
            }

        }, SPLASH_DURATION);
    }

    // ===================== REALTIME DB =====================

}