package com.campeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.campeat.app.model.MenuModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMenuAdminActivity extends AppCompatActivity {

    private EditText etMenuName, etPrice, etStock, etDescription;
    private Spinner spinnerCategory;
    private ImageView imgPhotoPreview;
    private FrameLayout framePhoto;
    private LinearLayout btnReplacePhoto, btnDeletePhoto;
    private LinearLayout layoutCustomizeOptions; // container list opsi
    private android.view.View layoutArchive;
    private AppCompatButton btnSaveChanges, btnCancel;

    private DatabaseReference menusRef;
    private Uri selectedImageUri = null;
    private String base64Image = null;

    // list customize options sementara sebelum disimpan
    private List<Map<String, Object>> customizeOptions = new ArrayList<>();

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            imgPhotoPreview.setImageURI(selectedImageUri);
                            imgPhotoPreview.setVisibility(ImageView.VISIBLE);
                            base64Image = convertImageToBase64(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_admin);

        menusRef = FirebaseDatabase.getInstance(
                ""
        ).getReference("menus");

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupCategorySpinner();
        setupClickListeners();
    }

    private void initViews() {
        etMenuName = findViewById(R.id.et_menu_name);
        etPrice = findViewById(R.id.et_price);
        etStock = findViewById(R.id.et_stock);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        imgPhotoPreview = findViewById(R.id.img_photo_preview);
        framePhoto = findViewById(R.id.frame_photo);
        btnReplacePhoto = findViewById(R.id.btn_replace_photo);
        btnDeletePhoto = findViewById(R.id.btn_delete_photo);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnCancel = findViewById(R.id.btn_cancel);
        layoutArchive = findViewById(R.id.tv_archive_menu);
        layoutCustomizeOptions = findViewById(R.id.layout_customize_options);
    }

    private void setupCategorySpinner() {
        FirebaseDatabase.getInstance(
                        ""
                ).getReference("categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String> categories = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String name = data.child("name").getValue(String.class);
                            if (name != null) categories.add(name);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AddMenuAdminActivity.this,
                                android.R.layout.simple_spinner_item,
                                categories);
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategory.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void setupClickListeners() {
        framePhoto.setOnClickListener(v -> openGallery());
        btnReplacePhoto.setOnClickListener(v -> openGallery());

        btnDeletePhoto.setOnClickListener(v -> {
            selectedImageUri = null;
            base64Image = null;
            imgPhotoPreview.setImageURI(null);
            imgPhotoPreview.setVisibility(ImageView.GONE);
            Toast.makeText(this, "Foto dihapus", Toast.LENGTH_SHORT).show();
        });

        // TAMBAH OPSI CUSTOMIZE
        findViewById(R.id.btn_tambah_opsi).setOnClickListener(v ->
                showTambahOpsiDialog()
        );

        btnSaveChanges.setOnClickListener(v -> saveMenu());
        btnCancel.setOnClickListener(v -> finish());
        layoutArchive.setOnClickListener(v ->
                Toast.makeText(this,
                        "Archive hanya tersedia saat edit menu",
                        Toast.LENGTH_SHORT).show());
    }

    // ================================
    // DIALOG TAMBAH OPSI CUSTOMIZE
    // ================================
    private void showTambahOpsiDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_tambah_opsi, null);

        EditText etLabel = dialogView.findViewById(R.id.et_opsi_label);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_opsi_type);
        EditText etChoices = dialogView.findViewById(R.id.et_opsi_choices);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Opsi Customize")
                .setView(dialogView)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    String label = etLabel.getText().toString().trim();
                    String choicesRaw = etChoices.getText().toString().trim();

                    if (label.isEmpty() || choicesRaw.isEmpty()) {
                        Toast.makeText(this,
                                "Label dan pilihan tidak boleh kosong",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedId = rgType.getCheckedRadioButtonId();
                    String type = selectedId == R.id.rb_single ? "single" : "multiple";

                    // parse choices dipisah koma
                    String[] choiceArr = choicesRaw.split(",");
                    Map<String, Object> choices = new HashMap<>();
                    for (String choice : choiceArr) {
                        String trimmed = choice.trim();
                        if (!trimmed.isEmpty()) {
                            String choiceKey = menusRef.push().getKey();
                            Map<String, Object> choiceMap = new HashMap<>();
                            choiceMap.put("name", trimmed);
                            choices.put(choiceKey != null ? choiceKey : trimmed, choiceMap);
                        }
                    }

                    Map<String, Object> option = new HashMap<>();
                    option.put("label", label);
                    option.put("type", type);
                    option.put("choices", choices);

                    customizeOptions.add(option);
                    addOpsiToLayout(label, type, choicesRaw);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ================================
    // TAMPILKAN OPSI DI LAYOUT
    // ================================
    private void addOpsiToLayout(String label, String type, String choices) {
        View opsiView = LayoutInflater.from(this)
                .inflate(R.layout.item_opsi_preview, layoutCustomizeOptions, false);

        TextView tvLabel = opsiView.findViewById(R.id.tv_opsi_label);
        TextView tvType = opsiView.findViewById(R.id.tv_opsi_type);
        TextView tvChoices = opsiView.findViewById(R.id.tv_opsi_choices);
        ImageView btnHapus = opsiView.findViewById(R.id.btn_hapus_opsi);

        tvLabel.setText(label);
        tvType.setText(type.equals("single") ? "Pilih Satu" : "Pilih Banyak");
        tvChoices.setText(choices);

        int index = customizeOptions.size() - 1;
        btnHapus.setOnClickListener(v -> {
            layoutCustomizeOptions.removeView(opsiView);
            if (index < customizeOptions.size()) {
                customizeOptions.remove(index);
            }
        });

        layoutCustomizeOptions.addView(opsiView);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

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

    private void saveMenu() {
        String name = etMenuName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Pilih kategori dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty()) {
            etMenuName.setError("Nama menu tidak boleh kosong");
            etMenuName.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Harga tidak boleh kosong");
            etPrice.requestFocus();
            return;
        }
        if (stockStr.isEmpty()) {
            etStock.setError("Stock tidak boleh kosong");
            etStock.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Deskripsi tidak boleh kosong");
            etDescription.requestFocus();
            return;
        }

        int price, stock;
        try {
            price = Integer.parseInt(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (Exception e) {
            Toast.makeText(this, "Harga / stock harus angka", Toast.LENGTH_SHORT).show();
            return;
        }

        MenuModel menu = new MenuModel();
        menu.setId((int) System.currentTimeMillis());
        menu.setName(name);
        menu.setPrice(price);
        menu.setStock(stock);
        menu.setCategory(category);
        menu.setDescription(description);
        menu.setImage(name.toLowerCase().replace(" ", "_"));
        menu.setImageBase64(base64Image);
        menu.setArchived(false);

        String newKey = menusRef.push().getKey();
        if (newKey == null) {
            Toast.makeText(this, "Gagal membuat key database", Toast.LENGTH_SHORT).show();
            return;
        }

        // simpan menu dulu
        menusRef.child(newKey).setValue(menu)
                .addOnSuccessListener(unused -> {

                    // simpan customize options kalau ada
                    if (!customizeOptions.isEmpty()) {
                        for (Map<String, Object> option : customizeOptions) {
                            String optKey = menusRef.child(newKey)
                                    .child("customizeOptions").push().getKey();
                            if (optKey != null) {
                                menusRef.child(newKey)
                                        .child("customizeOptions")
                                        .child(optKey)
                                        .setValue(option);
                            }
                        }
                    }

                    Toast.makeText(this,
                            name + " berhasil ditambahkan!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal menyimpan: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}