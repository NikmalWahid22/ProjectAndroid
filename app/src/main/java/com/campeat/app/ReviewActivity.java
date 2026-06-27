package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.campeat.app.model.ReviewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private ImageView btnBack;
    private ImageView star1, star2, star3, star4, star5;
    private EditText etReview;
    private AppCompatButton btnSubmit;

    // ================================
    // DATA
    // ================================
    private String menuKey;
    private String menuName;
    private int selectedRating = 0;

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getIntentData();
        initViews();
        setupStars();
        setupListeners();
    }

    // ================================
    // GET INTENT DATA
    // ================================
    private void getIntentData() {
        menuKey = getIntent().getStringExtra("menuKey");
        menuName = getIntent().getStringExtra("menuName");
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        etReview = findViewById(R.id.et_review);
        btnSubmit = findViewById(R.id.btn_submit);

        // set nama menu
        ((android.widget.TextView) findViewById(R.id.tv_food_name))
                .setText(menuName != null ? menuName : "Menu");
    }

    // ================================
    // SETUP STARS
    // ================================
    private ImageView[] stars;

    private void setupStars() {
        stars = new ImageView[]{star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating = rating;
                updateStars(rating);
            });
        }
    }

    private void updateStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
            }
        }
    }

    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    // ================================
    // SUBMIT REVIEW
    // ================================
    private void submitReview() {
        if (selectedRating == 0) {
            Toast.makeText(this, "Pilih rating dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = etReview.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Tulis ulasanmu dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (menuKey == null) {
            Toast.makeText(this, "Menu tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Login dulu untuk review", Toast.LENGTH_SHORT).show();
            return;
        }

        // ambil nama user dulu
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("users").child(uid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String username = snapshot.getValue(String.class);
                        if (username == null || username.isEmpty()) {
                            username = "User";
                        }
                        saveReview(uid, username, reviewText);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        saveReview(uid, "User", reviewText);
                    }
                });
    }

    // ================================
    // SAVE REVIEW KE FIREBASE
    // ================================
    private void saveReview(String uid, String username, String reviewText) {
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date());

        ReviewModel review = new ReviewModel(
                uid,
                username,
                selectedRating,
                reviewText,
                date
        );

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("ratings")
                .child(menuKey)
                .push()
                .setValue(review)
                .addOnSuccessListener(unused -> {
                    updateMenuRating();
                    Toast.makeText(this,
                            "Ulasan berhasil dikirim! Terima kasih",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal kirim ulasan: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
    // ================================
    // UPDATE RATING DI NODE MENU
    // ================================
    private void updateMenuRating() {
        DatabaseReference menuRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("menus").child(menuKey);

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("ratings").child(menuKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double total = 0;
                        int count = 0;

                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            Object value = reviewSnap.child("rating").getValue();

                            if (value instanceof Long) {
                                total += ((Long) value).doubleValue();
                                count++;
                            } else if (value instanceof Double) {
                                total += (Double) value;
                                count++;
                            } else if (value instanceof Integer) {
                                total += ((Integer) value).doubleValue();
                                count++;
                            } else if (value instanceof Float) {
                                total += ((Float) value).doubleValue();
                                count++;
                            } else if (value instanceof String) {
                                try {
                                    total += Double.parseDouble((String) value);
                                    count++;
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        if (count > 0) {
                            double avgRating = total / count;
                            menuRef.child("rating").setValue(avgRating);
                            menuRef.child("ratingCount").setValue(count);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
}