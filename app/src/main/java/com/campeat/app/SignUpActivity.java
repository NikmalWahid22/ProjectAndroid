package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText inputName, inputEmail, inputPassword;
    CheckBox checkboxTerms;
    Button buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputName = findViewById(R.id.input_name_register);
        inputEmail = findViewById(R.id.input_email_register);
        inputPassword = findViewById(R.id.input_password_register);
        checkboxTerms = findViewById(R.id.checkbox_terms);
        buttonSignUp = findViewById(R.id.button_sign_up_register);

        buttonSignUp.setOnClickListener(v -> {

            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (name.isEmpty()) {
                inputName.setError("Name required");
                return;
            }

            if (email.isEmpty()) {
                inputEmail.setError("Email required");
                return;
            }

            // 👉 Validasi Format Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.setError("Invalid email format");
                return;
            }

            if (password.isEmpty()) {
                inputPassword.setError("Password required");
                return;
            }

            if (!checkboxTerms.isChecked()) {
                Toast.makeText(this, "You must agree to Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Sign Up Success", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
            finish();
        });
    }
}
