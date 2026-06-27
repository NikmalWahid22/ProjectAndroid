package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private EditText inputEmail, inputPassword;
    private Button buttonSignIn;
    private TextView tvTogglePassword, textSignupLink, textForgotPassword;
    private Button btnGoogleSignIn;

    // ================================
    // FIREBASE
    // ================================
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    // ================================
    // GOOGLE SIGN IN
    // ================================
    private GoogleSignInClient googleSignInClient;
    private boolean isPasswordVisible = false;

    private final String DB_URL =
            "";

    // ================================
    // GOOGLE SIGN IN LAUNCHER
    // ================================
    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            Task<GoogleSignInAccount> task =
                                    GoogleSignIn.getSignedInAccountFromIntent(
                                            result.getData()
                                    );
                            try {
                                GoogleSignInAccount account =
                                        task.getResult(ApiException.class);
                                firebaseAuthWithGoogle(account.getIdToken());
                            } catch (ApiException e) {
                                Toast.makeText(this,
                                        "Google Sign-In gagal: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth  = FirebaseAuth.getInstance();
        dbRef  = FirebaseDatabase.getInstance(DB_URL).getReference();

        setupGoogleSignIn();
        initViews();
        setupListeners();
    }

    // ================================
    // SETUP GOOGLE SIGN IN
    // ================================
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        inputEmail         = findViewById(R.id.input_email);
        inputPassword      = findViewById(R.id.input_password);
        buttonSignIn       = findViewById(R.id.button_sign_in);
        tvTogglePassword   = findViewById(R.id.tv_toggle_password);
        textSignupLink     = findViewById(R.id.text_signup_link);
        textForgotPassword = findViewById(R.id.text_forgot_password);
        btnGoogleSignIn    = findViewById(R.id.btn_google_sign_in);
    }

    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {
        // Toggle password
        tvTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                inputPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance());
                tvTogglePassword.setText("👁️");
            } else {
                inputPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance());
                tvTogglePassword.setText("🙈");
            }
            isPasswordVisible = !isPasswordVisible;
            inputPassword.setSelection(inputPassword.getText().length());
        });

        // Sign In email/password
        buttonSignIn.setOnClickListener(v -> attemptLogin());

        // Google Sign In
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        // Go to Register
        textSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        // Forgot Password
        textForgotPassword.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this,
                        "Masukkan email kamu dulu",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this,
                            "Email reset dikirim ke " + email,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this,
                            "Gagal kirim email reset",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ================================
    // ATTEMPT LOGIN
    // ================================
    private void attemptLogin() {
        String email    = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("Email tidak boleh kosong");
            inputEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Format email tidak valid");
            inputEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            inputPassword.setError("Password tidak boleh kosong");
            inputPassword.requestFocus();
            return;
        }

        buttonSignIn.setEnabled(false);
        buttonSignIn.setText("Memuat...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    checkUserRole(uid);
                })
                .addOnFailureListener(e -> {
                    buttonSignIn.setEnabled(true);
                    buttonSignIn.setText("Sign In");
                    Toast.makeText(this,
                            "Email / Password salah",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ================================
    // FIREBASE AUTH WITH GOOGLE
    // ================================
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    String uid = user.getUid();

                    // Cek apakah user sudah ada di database
                    dbRef.child("users").child(uid).child("role")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // User sudah ada → langsung routing
                                        checkUserRole(uid);
                                    } else {
                                        // User baru → simpan dulu ke database
                                        saveGoogleUserToDatabase(user);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(SignInActivity.this,
                                            "Gagal cek data user",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Google Sign-In gagal: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // SAVE GOOGLE USER KE DATABASE
    // ================================
    private void saveGoogleUserToDatabase(FirebaseUser user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("users/" + user.getUid() + "/role",        "user");
        updates.put("users/" + user.getUid() + "/name",        user.getDisplayName() != null ? user.getDisplayName() : "User");
        updates.put("users/" + user.getUid() + "/email",       user.getEmail() != null ? user.getEmail() : "");
        updates.put("users/" + user.getUid() + "/phone",       "");
        updates.put("users/" + user.getUid() + "/imageBase64", "");
        updates.put("users/" + user.getUid() + "/point",       0);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Selamat datang, " + user.getDisplayName() + "! 🎉",
                            Toast.LENGTH_SHORT).show();
                    // Google user selalu role "user"
                    navigateTo(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal simpan data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // CHECK USER ROLE
    // ================================
    private void checkUserRole(String uid) {
        dbRef.child("users").child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        buttonSignIn.setEnabled(true);
                        buttonSignIn.setText("Sign In");

                        String role = snapshot.getValue(String.class);
                        navigateTo("admin".equals(role));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        buttonSignIn.setEnabled(true);
                        buttonSignIn.setText("Sign In");
                        Toast.makeText(SignInActivity.this,
                                "Gagal membaca role",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================================
    // NAVIGATE TO HOME
    // ================================
    private void navigateTo(boolean isAdmin) {
        Intent intent = isAdmin
                ? new Intent(this, HomeAdminActivity.class)
                : new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}