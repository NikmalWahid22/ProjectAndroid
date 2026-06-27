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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.CategoryAdapter;
import com.campeat.app.model.CategoryModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileAdminActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private ShapeableImageView imgAdminPhoto;
    private ShapeableImageView btnEditPhoto;
    private TextView tvAdminName;
    private TextView tvAdminEmail;
    private TextView tvTotalOrderCount;
    private TextView tvTotalMenuCount;
    private LinearLayout btnEditProfile;
    private LinearLayout navHome, navOrders, navProfile;
    private AppCompatButton btnLogout;
    private RecyclerView rvCategories;

    // ================================
    // FIREBASE
    // ================================
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private String adminUid;

    // ================================
    // KATEGORI
    // ================================
    private List<CategoryModel> categoryList;
    private CategoryAdapter categoryAdapter;

    // ================================
    // IMAGE
    // ================================
    private String currentBase64Photo = null;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Uri uri = result.getData().getData();
                            String base64 = convertImageToBase64(uri);

                            if (base64 != null) {
                                // tampilkan preview
                                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                imgAdminPhoto.setImageBitmap(bitmap);

                                // simpan ke Firebase
                                savePhotoToFirebase(base64);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        adminUid = user.getUid();

        dbRef = FirebaseDatabase.getInstance(
                ""
        ).getReference();

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        loadAdminData();
        loadTotalOrder();
        loadTotalMenu();
        loadCategories();
        setupClickListeners();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        imgAdminPhoto = findViewById(R.id.img_admin_photo);
        btnEditPhoto = findViewById(R.id.btn_edit_photo);
        tvAdminName = findViewById(R.id.tv_admin_name);
        tvAdminEmail = findViewById(R.id.tv_admin_email);
        tvTotalOrderCount = findViewById(R.id.tv_total_order_count);
        tvTotalMenuCount = findViewById(R.id.tv_total_menu_count);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        rvCategories = findViewById(R.id.rv_categories);
        navHome = findViewById(R.id.nav_home);
        navOrders = findViewById(R.id.nav_orders);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(
                this,
                categoryList,
                category -> showDeleteCategoryDialog(category)
        );
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setNestedScrollingEnabled(false);
        rvCategories.setAdapter(categoryAdapter);
    }

    // ================================
    // SETUP BOTTOM NAV
    // ================================
    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeAdminActivity.class));
            finish();
        });

        navOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageMenuAdminActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "Anda sudah di halaman Profile", Toast.LENGTH_SHORT).show()
        );
    }

    // ================================
    // SETUP CLICK LISTENERS
    // ================================
    private void setupClickListeners() {

        // EDIT FOTO
        btnEditPhoto.setOnClickListener(v -> openGallery());
        imgAdminPhoto.setOnClickListener(v -> openGallery());

        // EDIT PROFILE
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // TAMBAH KATEGORI
        findViewById(R.id.btn_tambah_kategori).setOnClickListener(v ->
                showTambahKategoriDialog()
        );

        findViewById(R.id.btn_manage_banner).setOnClickListener(v -> {
            startActivity(new Intent(this, ManageBannerAdminActivity.class));
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ================================
    // LOAD ADMIN DATA
    // ================================
    private void loadAdminData() {
        dbRef.child("admins").child(adminUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String photoBase64 = snapshot.child("imageBase64").getValue(String.class);

                        if (name != null) tvAdminName.setText(name);
                        if (email != null) tvAdminEmail.setText(email);

                        // load foto
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            currentBase64Photo = photoBase64;
                            byte[] decoded = Base64.decode(photoBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            imgAdminPhoto.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ProfileAdminActivity.this,
                                "Gagal load data admin", Toast.LENGTH_SHORT).show();
                    }
                });

        // fallback email dari Firebase Auth
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvAdminEmail.setText(user.getEmail());
        }
    }

    // ================================
    // LOAD TOTAL ORDER
    // ================================
    private void loadTotalOrder() {
        dbRef.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalOrder = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    totalOrder += (int) userSnapshot.getChildrenCount();
                }
                tvTotalOrderCount.setText(String.valueOf(totalOrder));
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ================================
    // LOAD TOTAL MENU
    // ================================
    private void loadTotalMenu() {
        dbRef.child("menus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tvTotalMenuCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ================================
    // LOAD CATEGORIES
    // ================================
    private void loadCategories() {
        dbRef.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CategoryModel category = dataSnapshot.getValue(CategoryModel.class);
                    if (category != null) {
                        category.setKey(dataSnapshot.getKey());
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileAdminActivity.this,
                        "Gagal load kategori", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================================
    // DIALOG TAMBAH KATEGORI
    // ================================
    private void showTambahKategoriDialog() {
        EditText etKategori = new EditText(this);
        etKategori.setHint("Nama kategori");

        new AlertDialog.Builder(this)
                .setTitle("Tambah Kategori")
                .setView(etKategori)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    String nama = etKategori.getText().toString().trim();
                    if (nama.isEmpty()) {
                        Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tambahKategori(nama);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void tambahKategori(String nama) {
        String key = dbRef.child("categories").push().getKey();
        if (key == null) return;

        CategoryModel category = new CategoryModel(nama);

        dbRef.child("categories").child(key).setValue(category)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal tambah kategori", Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // DIALOG HAPUS KATEGORI
    // ================================
    private void showDeleteCategoryDialog(CategoryModel category) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Kategori")
                .setMessage("Yakin mau hapus kategori \"" + category.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteCategory(CategoryModel category) {
        if (category.getKey() == null) return;

        dbRef.child("categories").child(category.getKey())
                .removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal hapus kategori", Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // DIALOG EDIT PROFILE
    // ================================
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile_admin, null);
        EditText etName = dialogView.findViewById(R.id.et_edit_name);

        // isi nama sekarang
        etName.setText(tvAdminName.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveNameToFirebase(newName);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveNameToFirebase(String name) {
        dbRef.child("admins").child(adminUid).child("name").setValue(name)
                .addOnSuccessListener(unused -> {
                    tvAdminName.setText(name);
                    Toast.makeText(this, "Nama berhasil diupdate", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal update nama", Toast.LENGTH_SHORT).show()
                );
    }

    // ================================
    // OPEN GALLERY
    // ================================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // ================================
    // SAVE PHOTO KE FIREBASE
    // ================================
    private void savePhotoToFirebase(String base64) {
        dbRef.child("admins").child(adminUid).child("imageBase64").setValue(base64)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Foto berhasil diupdate", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal update foto", Toast.LENGTH_SHORT).show()
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
    // DIALOG LOGOUT
    // ================================
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin mau logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}