package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPhone, inputPassword;
    private Button buttonRegister;
    private CheckBox checkboxTerms;
    private TextView tvTogglePassword, textSigninLink;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance(
                ""
        ).getReference();

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupListeners();
    }

    private void initViews() {
        inputName        = findViewById(R.id.input_name);
        inputEmail       = findViewById(R.id.input_email);
        inputPhone       = findViewById(R.id.input_phone);
        inputPassword    = findViewById(R.id.input_password);
        buttonRegister   = findViewById(R.id.button_register);
        checkboxTerms    = findViewById(R.id.checkbox_terms);
        tvTogglePassword = findViewById(R.id.tv_toggle_password);
        textSigninLink   = findViewById(R.id.text_signin_link);
    }

    private void setupListeners() {
        // Toggle password visibility
        tvTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tvTogglePassword.setText("👁️");
            } else {
                inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                tvTogglePassword.setText("🙈");
            }
            isPasswordVisible = !isPasswordVisible;
            inputPassword.setSelection(inputPassword.getText().length());
        });

        // Register
        buttonRegister.setOnClickListener(v -> attemptRegister());

        // Back to Sign In
        textSigninLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name     = inputName.getText().toString().trim();
        String email    = inputEmail.getText().toString().trim();
        String phone    = inputPhone.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (name.isEmpty()) {
            inputName.setError("Nama tidak boleh kosong");
            inputName.requestFocus(); return;
        }
        if (email.isEmpty()) {
            inputEmail.setError("Email tidak boleh kosong");
            inputEmail.requestFocus(); return;
        }
        if (phone.isEmpty()) {
            inputPhone.setError("Nomor telepon tidak boleh kosong");
            inputPhone.requestFocus(); return;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Password tidak boleh kosong");
            inputPassword.requestFocus(); return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Password minimal 6 karakter");
            inputPassword.requestFocus(); return;
        }
        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "Kamu harus menyetujui Terms & Condition", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonRegister.setEnabled(false);
        buttonRegister.setText("Mendaftar...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) saveUserToDatabase(user.getUid(), name, email, phone);
                    } else {
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Sign Up");
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Registrasi gagal";
                        Toast.makeText(this, "Gagal: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String uid, String name, String email, String phone) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("users/" + uid + "/role",        "user");
        updates.put("users/" + uid + "/name",        name);
        updates.put("users/" + uid + "/email",       email);
        updates.put("users/" + uid + "/phone",       phone);
        updates.put("users/" + uid + "/imageBase64", "");
        updates.put("users/" + uid + "/point",       0);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Akun berhasil dibuat! 🎉", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Sign Up");
                    Toast.makeText(this, "Gagal simpan data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}