package com.campeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private ShapeableImageView imgProfilePhoto;
    private ShapeableImageView btnEditPhoto;
    private TextView tvUserName, tvUserEmail;
    private TextView tvPointCount, tvRank;
    private LinearLayout btnEditProfile, btnOrderHistory;
    private AppCompatButton btnLogout;
    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // FIREBASE
    // ================================
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private String uid;

    // ================================
    // IMAGE
    // ================================
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            String base64 = convertImageToBase64(uri);
                            if (base64 != null) {
                                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(
                                        decoded, 0, decoded.length);
                                imgProfilePhoto.setImageBitmap(bitmap);
                                savePhotoToFirebase(base64);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        uid = user.getUid();
        dbRef = FirebaseDatabase.getInstance(
                ""
        ).getReference();

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        loadProfile();
        setupClickListeners();
        setupBottomNav();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        imgProfilePhoto = findViewById(R.id.img_profile_photo);
        btnEditPhoto = findViewById(R.id.btn_edit_photo);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvPointCount = findViewById(R.id.tv_point_count);
        tvRank = findViewById(R.id.tv_rank);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnOrderHistory = findViewById(R.id.btn_order_history);
        btnLogout = findViewById(R.id.btn_logout);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // LOAD PROFILE
    // ================================
    private void loadProfile() {
        dbRef.child("users").child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    Integer point = snapshot.child("point").getValue(Integer.class);
                    String photoBase64 = snapshot.child("imageBase64").getValue(String.class);

                    if (name == null || name.isEmpty()) name = "User";
                    if (point == null) point = 0;

                    tvUserName.setText(name);
                    tvUserEmail.setText(email != null ? email :
                            auth.getCurrentUser().getEmail());
                    tvPointCount.setText(String.valueOf(point));
                    tvRank.setText(getRank(point));

                    // load foto
                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        byte[] decoded = Base64.decode(photoBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                decoded, 0, decoded.length);
                        imgProfilePhoto.setImageBitmap(bitmap);
                    }
                });
    }

    // ================================
    // GET RANK
    // ================================
    private String getRank(int point) {
        if (point >= 3000) return "🏆 Platinum";
        if (point >= 2000) return "🥇 Gold";
        if (point >= 1000) return "🥈 Silver";
        return "🥉 Bronze";
    }

    // ================================
    // SETUP CLICK LISTENERS
    // ================================
    private void setupClickListeners() {
        btnEditPhoto.setOnClickListener(v -> openGallery());
        imgProfilePhoto.setOnClickListener(v -> openGallery());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class))
        );

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ================================
    // SETUP BOTTOM NAV
    // ================================
    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });

        navSearch.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, SearchActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });

        navCart.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "Anda sudah di Profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void animateNavClick(View view) {
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .alpha(0.82f)
                .setDuration(70)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(130)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.4f))
                        .start())
                .start();
    }

    // ================================
    // EDIT PROFILE DIALOG
    // ================================
    // ================================
    // EDIT PROFILE DIALOG
    // ================================
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_edit_profile, null);

        EditText etName  = dialogView.findViewById(R.id.et_edit_name);
        EditText etPhone = dialogView.findViewById(R.id.et_edit_phone);
        Button   btnSave   = dialogView.findViewById(R.id.btn_save);
        Button   btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Pre-fill nama
        etName.setText(tvUserName.getText().toString());

        // Pre-fill phone dari Firebase
        dbRef.child("users").child(uid).child("phone").get()
                .addOnSuccessListener(snapshot -> {
                    String phone = snapshot.getValue(String.class);
                    if (phone != null) etPhone.setText(phone);
                });

        // Buat dialog tanpa button bawaan AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Background dialog transparan supaya CardView rounded kelihatan
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName  = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (newName.isEmpty()) {
                etName.setError("Nama tidak boleh kosong");
                etName.requestFocus();
                return;
            }

            dbRef.child("users").child(uid).child("name").setValue(newName);
            dbRef.child("users").child(uid).child("phone").setValue(newPhone)
                    .addOnSuccessListener(unused -> {
                        tvUserName.setText(newName);
                        Toast.makeText(this,
                                "Profile berhasil diupdate ✅",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    // ================================
    // LOGOUT DIALOG
    // ================================
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin mau logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ================================
    // OPEN GALLERY
    // ================================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // ================================
    // SAVE PHOTO
    // ================================
    private void savePhotoToFirebase(String base64) {
        dbRef.child("users").child(uid).child("imageBase64").setValue(base64)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Foto berhasil diupdate",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // KONVERSI GAMBAR KE BASE64
    // ================================
    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================================
    // REFRESH SAAT BALIK
    // ================================
    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }
}