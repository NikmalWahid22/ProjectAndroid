package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            finish();
        });
    }

}