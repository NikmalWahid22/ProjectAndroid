package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button buttonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        buttonSignIn = findViewById(R.id.button_sign_in);

        buttonSignIn.setOnClickListener(view -> {

            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                inputEmail.setError("Email tidak boleh kosong");
                return;
            }

            // 👉 Validasi email HARUS ada @ dan format benar
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Format email tidak valid");
                return;
            }

            if (password.isEmpty()) {
                inputPassword.setError("Password tidak boleh kosong");
                return;
            }

            Toast.makeText(this, "Sign In berhasil", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
            finish();
        });
    }
}
