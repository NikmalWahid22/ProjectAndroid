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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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

public class EditMenuAdminActivity extends AppCompatActivity {

    private EditText etMenuName, etPrice, etStock, etDescription;
    private Spinner spinnerCategory;
    private ImageView imgPhotoPreview;
    private FrameLayout framePhoto;
    private LinearLayout btnReplacePhoto, btnDeletePhoto, layoutArchive;
    private LinearLayout layoutPhotoPlaceholder;
    private LinearLayout layoutCustomizeOptions;
    private AppCompatButton btnSaveChanges, btnCancel;

    private DatabaseReference menusRef;

    private String menuKey;
    private String currentImage;
    private String base64Image = null;
    private String currentBase64 = null;
    private String currentCategory = null;

    private Uri selectedImageUri = null;
    private List<String> categoryList = new ArrayList<>();
    private List<Map<String, Object>> customizeOptions = new ArrayList<>();

    // ================================
    // GALLERY LAUNCHER
    // ================================
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            imgPhotoPreview.setImageURI(selectedImageUri);
                            imgPhotoPreview.setVisibility(ImageView.VISIBLE);
                            layoutPhotoPlaceholder.setVisibility(View.GONE);
                            base64Image = convertImageToBase64(selectedImageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu_admin);

        menusRef = FirebaseDatabase
                .getInstance("")
                .getReference("menus");

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        loadIntentData();
        setupCategorySpinner();
        loadExistingCustomizeOptions();
        setupClickListeners();
    }

    // ================================
    // INIT VIEWS
    // ================================
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
        layoutArchive = findViewById(R.id.layout_archive);
        layoutPhotoPlaceholder = findViewById(R.id.layout_photo_placeholder);
        layoutCustomizeOptions = findViewById(R.id.layout_customize_options);
    }

    // ================================
    // LOAD INTENT DATA
    // ================================
    private void loadIntentData() {
        Intent intent = getIntent();
        menuKey = intent.getStringExtra("menuKey");
        String menuName = intent.getStringExtra("menuName");
        int menuPrice = intent.getIntExtra("menuPrice", 0);
        int menuStock = intent.getIntExtra("menuStock", 0);
        String menuDescription = intent.getStringExtra("menuDescription");
        currentCategory = intent.getStringExtra("menuCategory");
        currentImage = intent.getStringExtra("menuImage");
        currentBase64 = intent.getStringExtra("menuImageBase64");

        etMenuName.setText(menuName);
        etPrice.setText(String.valueOf(menuPrice));
        etStock.setText(String.valueOf(menuStock));
        etDescription.setText(menuDescription);

        if (currentBase64 != null && !currentBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(currentBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgPhotoPreview.setImageBitmap(bitmap);
            imgPhotoPreview.setVisibility(ImageView.VISIBLE);
            layoutPhotoPlaceholder.setVisibility(View.GONE);
        }
    }

    // ================================
    // CATEGORY SPINNER — dari Firebase
    // ================================
    private void setupCategorySpinner() {
        FirebaseDatabase.getInstance(
                        ""
                ).getReference("categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        categoryList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String name = data.child("name").getValue(String.class);
                            if (name != null) categoryList.add(name);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                EditMenuAdminActivity.this,
                                android.R.layout.simple_spinner_item,
                                categoryList);
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategory.setAdapter(adapter);

                        // set selection sesuai kategori menu
                        if (currentCategory != null) {
                            for (int i = 0; i < categoryList.size(); i++) {
                                if (categoryList.get(i).equalsIgnoreCase(currentCategory)) {
                                    spinnerCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================================
    // LOAD EXISTING CUSTOMIZE OPTIONS
    // ================================
    private void loadExistingCustomizeOptions() {
        menusRef.child(menuKey).child("customizeOptions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutCustomizeOptions.removeAllViews();
                        customizeOptions.clear();

                        for (DataSnapshot optSnap : snapshot.getChildren()) {
                            String label = optSnap.child("label").getValue(String.class);
                            String type = optSnap.child("type").getValue(String.class);
                            String optKey = optSnap.getKey();

                            StringBuilder choicesSb = new StringBuilder();
                            for (DataSnapshot choiceSnap : optSnap.child("choices").getChildren()) {
                                String cName = choiceSnap.child("name").getValue(String.class);
                                if (cName != null) {
                                    if (choicesSb.length() > 0) choicesSb.append(", ");
                                    choicesSb.append(cName);
                                }
                            }

                            // rebuild map
                            Map<String, Object> optMap = new HashMap<>();
                            optMap.put("label", label);
                            optMap.put("type", type);
                            optMap.put("_key", optKey);
                            customizeOptions.add(optMap);

                            if (label != null && type != null) {
                                addOpsiToLayout(label, type, choicesSb.toString(), optKey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
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

                    // langsung simpan ke Firebase
                    String optKey = menusRef.child(menuKey)
                            .child("customizeOptions").push().getKey();
                    if (optKey != null) {
                        Map<String, Object> option = new HashMap<>();
                        option.put("label", label);
                        option.put("type", type);
                        option.put("choices", choices);

                        menusRef.child(menuKey)
                                .child("customizeOptions")
                                .child(optKey)
                                .setValue(option)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(this,
                                                "Opsi berhasil ditambahkan",
                                                Toast.LENGTH_SHORT).show()
                                );

                        Map<String, Object> optMap = new HashMap<>(option);
                        optMap.put("_key", optKey);
                        customizeOptions.add(optMap);
                        addOpsiToLayout(label, type, choicesRaw, optKey);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ================================
    // TAMPILKAN OPSI DI LAYOUT
    // ================================
    private void addOpsiToLayout(String label, String type, String choices, String optKey) {
        View opsiView = LayoutInflater.from(this)
                .inflate(R.layout.item_opsi_preview, layoutCustomizeOptions, false);

        TextView tvLabel = opsiView.findViewById(R.id.tv_opsi_label);
        TextView tvType = opsiView.findViewById(R.id.tv_opsi_type);
        TextView tvChoices = opsiView.findViewById(R.id.tv_opsi_choices);
        ImageView btnHapus = opsiView.findViewById(R.id.btn_hapus_opsi);

        tvLabel.setText(label);
        tvType.setText(type.equals("single") ? "Pilih Satu" : "Pilih Banyak");
        tvChoices.setText(choices);

        btnHapus.setOnClickListener(v -> {
            // hapus dari Firebase
            menusRef.child(menuKey)
                    .child("customizeOptions")
                    .child(optKey)
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        layoutCustomizeOptions.removeView(opsiView);
                        Toast.makeText(this,
                                "Opsi dihapus", Toast.LENGTH_SHORT).show();
                    });
        });

        layoutCustomizeOptions.addView(opsiView);
    }

    // ================================
    // CLICK LISTENERS
    // ================================
    private void setupClickListeners() {
        framePhoto.setOnClickListener(v -> openGallery());
        btnReplacePhoto.setOnClickListener(v -> openGallery());

        btnDeletePhoto.setOnClickListener(v -> {
            selectedImageUri = null;
            base64Image = null;
            currentBase64 = null;
            imgPhotoPreview.setImageURI(null);
            imgPhotoPreview.setImageBitmap(null);
            imgPhotoPreview.setVisibility(ImageView.GONE);
            layoutPhotoPlaceholder.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Foto dihapus", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_tambah_opsi).setOnClickListener(v ->
                showTambahOpsiDialog()
        );

        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> finish());
        layoutArchive.setOnClickListener(v -> showArchiveDialog());
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

    private void saveChanges() {
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

        int price = Integer.parseInt(priceStr);
        int stock = Integer.parseInt(stockStr);
        String finalBase64 = (base64Image != null) ? base64Image : currentBase64;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("stock", stock);
        updates.put("description", description);
        updates.put("category", category);
        updates.put("image", currentImage);
        updates.put("imageBase64", finalBase64);

        menusRef.child(menuKey)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            name + " berhasil diupdate!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void showArchiveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Archive Menu")
                .setMessage("Yakin mau archive menu ini?")
                .setPositiveButton("Archive", (dialog, which) -> archiveMenu())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void archiveMenu() {
        menusRef.child(menuKey).child("archived").setValue(true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Menu berhasil diarchive",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal archive menu",
                                Toast.LENGTH_SHORT).show());
    }
}