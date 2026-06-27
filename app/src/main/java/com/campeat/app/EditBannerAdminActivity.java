package com.campeat.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.campeat.app.model.BannerModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditBannerAdminActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 200;

    private ImageView imgBannerPreview;

    private EditText etBannerTitle;
    private EditText etBannerDescription;
    private EditText etBannerTag;

    private AppCompatButton btnSaveBanner;
    private AppCompatButton btnCancel;

    private LinearLayout btnReplacePhoto;
    private LinearLayout btnDeletePhoto;
    private FrameLayout framePhoto;
    private SwitchMaterial switchActiveBanner;

    private String imageBase64 = "";
    private String bannerKey = "";

    private DatabaseReference bannerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_banner_admin);

        initViews();

        bannerRef = FirebaseDatabase.getInstance(
                ""
        ).getReference("banners");

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getBannerData();

        setupAction();
    }

    private void initViews() {

        imgBannerPreview = findViewById(R.id.img_banner_preview);

        etBannerTitle = findViewById(R.id.et_banner_title);
        etBannerDescription = findViewById(R.id.et_banner_description);
        etBannerTag = findViewById(R.id.et_banner_tag);

        btnSaveBanner = findViewById(R.id.btn_save_banner);
        btnCancel = findViewById(R.id.btn_cancel);

        btnReplacePhoto = findViewById(R.id.btn_replace_photo);
        btnDeletePhoto = findViewById(R.id.btn_delete_photo);

        framePhoto = findViewById(R.id.frame_photo);

        btnSaveBanner.setText("Update Banner");
    }

    private void getBannerData() {

        bannerKey = getIntent().getStringExtra("bannerKey");

        String title =
                getIntent().getStringExtra("bannerTitle");

        String description =
                getIntent().getStringExtra("bannerDescription");

        String tag =
                getIntent().getStringExtra("bannerTag");

        imageBase64 =
                getIntent().getStringExtra("bannerImage");

        etBannerTitle.setText(title);
        etBannerDescription.setText(description);
        etBannerTag.setText(tag);

        boolean active = getIntent().getBooleanExtra("bannerActive", true);
        switchActiveBanner.setChecked(active);

        try {

            byte[] bytes =
                    Base64.decode(imageBase64, Base64.DEFAULT);

            Bitmap bitmap =
                    BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.length
                    );

            imgBannerPreview.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAction() {

        framePhoto.setOnClickListener(v -> openGallery());

        btnReplacePhoto.setOnClickListener(v -> openGallery());

        btnDeletePhoto.setOnClickListener(v -> {

            imgBannerPreview.setImageDrawable(null);

            imageBase64 = "";

            Toast.makeText(
                    this,
                    "Banner image removed",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnSaveBanner.setOnClickListener(v -> updateBanner());
    }

    private void openGallery() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri imageUri = data.getData();

            try {

                Bitmap bitmap;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    ImageDecoder.Source source =
                            ImageDecoder.createSource(
                                    getContentResolver(),
                                    imageUri
                            );

                    bitmap = ImageDecoder.decodeBitmap(source);

                } else {

                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            imageUri
                    );
                }

                imgBannerPreview.setImageBitmap(bitmap);

                imageBase64 = convertToBase64(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] imageBytes = baos.toByteArray();

        return Base64.encodeToString(
                imageBytes,
                Base64.DEFAULT
        );
    }

    private void updateBanner() {

        String title =
                etBannerTitle.getText().toString().trim();

        String description =
                etBannerDescription.getText().toString().trim();

        String tag =
                etBannerTag.getText().toString().trim();

        if (title.isEmpty()) {
            etBannerTitle.setError("Title required");
            return;
        }

        if (description.isEmpty()) {
            etBannerDescription.setError("Description required");
            return;
        }

        if (tag.isEmpty()) {
            etBannerTag.setError("Tag required");
            return;
        }

        if (imageBase64.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please upload banner image",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating banner...");
        dialog.setCancelable(false);
        dialog.show();

        boolean active = switchActiveBanner.isChecked();

        BannerModel bannerModel = new BannerModel(
                title,
                description,
                tag,
                imageBase64,
                active
        );

        bannerRef.child(bannerKey)
                .setValue(bannerModel)
                .addOnSuccessListener(unused -> {

                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            "Banner updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}