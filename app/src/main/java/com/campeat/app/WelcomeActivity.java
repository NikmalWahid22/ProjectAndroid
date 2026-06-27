package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Kalau user sudah login
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish();
            return;
        }

        // Memuat layout activity_welcome.xml
        setContentView(R.layout.activity_welcome);

        // Inisialisasi Tombol (ID dari XML yang baru saja Anda berikan)
        Button btnSignIn = findViewById(R.id.button_sign_in);
        Button btnSignUp = findViewById(R.id.button_sign_up);

        // Listener untuk Sign In
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Listener untuk Sign Up
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
