package com.campeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    // ================================
    // DATA
    // ================================
    private String foodId;
    private String foodName;
    private String foodDesc;
    private String foodCategory;
    private int foodPrice;
    private String foodImage;

    private int quantity = 1;
    private boolean isWishlisted = false;

    // ================================
    // VIEWS
    // ================================
    private ImageView imageFood;
    private TextView textName, textDesc, textPrice;
    private TextView tvQuantity, tvRatingValue, tvRatingCount;
    private ImageView btnBack, btnMinus, btnPlus, btnWishlist;
    private AppCompatButton btnAddToCart;
    private EditText etNotes;
    private LinearLayout layoutCustomizeContainer;
    private TextView tvCustomizeTitle;

    // ================================
    // FIREBASE
    // ================================
    private DatabaseReference dbRef;
    private String uid;

    // ================================
    // CUSTOMIZE OPTIONS
    // ================================
    private Map<String, List<String>> selectedOptions = new HashMap<>();

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // GET INTENT DATA
        Intent intent = getIntent();
        foodId       = intent.getStringExtra("menuKey");
        foodName     = intent.getStringExtra("menuName");
        foodDesc     = intent.getStringExtra("menuDescription");
        foodCategory = intent.getStringExtra("menuCategory");
        foodImage    = intent.getStringExtra("menuImageBase64");
        foodPrice    = intent.getIntExtra("menuPrice", 0);
        if (foodPrice == 0) {
            foodPrice = (int) intent.getDoubleExtra("menuPrice", 0);
        }

        if (foodId == null) {
            Toast.makeText(this, "Data makanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        loadData();
        loadCustomizeOptions();
        loadRatings();
        checkWishlist();
        setupClickListeners();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        imageFood               = findViewById(R.id.img_food_detail);
        textName                = findViewById(R.id.tv_food_name);
        textDesc                = findViewById(R.id.tv_description);
        textPrice               = findViewById(R.id.tv_food_price);
        tvQuantity              = findViewById(R.id.tv_quantity);
        btnBack                 = findViewById(R.id.btn_back);
        btnMinus                = findViewById(R.id.btn_minus);
        btnPlus                 = findViewById(R.id.btn_plus);
        btnWishlist             = findViewById(R.id.btn_wishlist);
        btnAddToCart            = findViewById(R.id.btn_add_to_cart);
        etNotes                 = findViewById(R.id.et_notes);
        layoutCustomizeContainer = findViewById(R.id.layout_customize_container);
        tvCustomizeTitle        = findViewById(R.id.tv_customize_title);
        tvRatingValue           = findViewById(R.id.tv_rating_value);
        tvRatingCount           = findViewById(R.id.tv_rating_count);
    }

    // ================================
    // LOAD DATA
    // ================================
    private void loadData() {
        textName.setText(foodName != null ? foodName : "");
        textDesc.setText(foodDesc != null ? foodDesc : "");

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textPrice.setText(format.format(foodPrice));

        if (foodImage != null && !foodImage.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(foodImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageFood.setImageBitmap(bitmap);
            } catch (Exception e) {
                imageFood.setImageResource(R.drawable.img_placeholder);
            }
        } else {
            imageFood.setImageResource(R.drawable.img_placeholder);
        }
    }

    // ================================
    // LOAD RATINGS
    // ================================
    private void loadRatings() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("ratings")
                .child(foodId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        float totalRating = 0;
                        int totalReview = 0;

                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            Float rating = reviewSnap.child("rating").getValue(Float.class);
                            if (rating != null) {
                                totalRating += rating;
                                totalReview++;
                            }
                        }

                        if (totalReview > 0) {
                            float average = totalRating / totalReview;
                            tvRatingValue.setText(String.format("%.1f", average));
                            tvRatingCount.setText("(" + totalReview + " Reviews)");
                        } else {
                            tvRatingValue.setText("0.0");
                            tvRatingCount.setText("(0 Reviews)");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================================
    // LOAD CUSTOMIZE OPTIONS
    // ================================
    private void loadCustomizeOptions() {
        dbRef.child("menus").child(foodId).child("customizeOptions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutCustomizeContainer.removeAllViews();

                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            tvCustomizeTitle.setVisibility(View.GONE);
                            return;
                        }

                        tvCustomizeTitle.setVisibility(View.VISIBLE);

                        for (DataSnapshot optSnap : snapshot.getChildren()) {
                            String label  = optSnap.child("label").getValue(String.class);
                            String type   = optSnap.child("type").getValue(String.class);
                            List<String> choices = new ArrayList<>();

                            for (DataSnapshot choiceSnap : optSnap.child("choices").getChildren()) {
                                String name = choiceSnap.child("name").getValue(String.class);
                                if (name != null) choices.add(name);
                            }

                            if (label != null && type != null && !choices.isEmpty()) {
                                if (type.equals("single")) {
                                    addSingleChoiceView(label, choices);
                                } else {
                                    addMultipleChoiceView(label, choices);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================================
    // SINGLE CHOICE
    // ================================
    private void addSingleChoiceView(String label, List<String> choices) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_customize_single, layoutCustomizeContainer, false);

        TextView tvLabel = view.findViewById(R.id.tv_customize_label);
        RadioGroup radioGroup = view.findViewById(R.id.rg_choices);
        tvLabel.setText(label);
        selectedOptions.put(label, new ArrayList<>());

        for (String choice : choices) {
            RadioButton rb = new RadioButton(this);
            rb.setText(choice);
            rb.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            rb.setTextSize(13);
            radioGroup.addView(rb);
        }

        if (radioGroup.getChildCount() > 0) {
            ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
            List<String> defaultSelected = new ArrayList<>();
            defaultSelected.add(choices.get(0));
            selectedOptions.put(label, defaultSelected);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = group.findViewById(checkedId);
            if (selected != null) {
                List<String> sel = new ArrayList<>();
                sel.add(selected.getText().toString());
                selectedOptions.put(label, sel);
            }
        });

        layoutCustomizeContainer.addView(view);
    }

    // ================================
    // MULTIPLE CHOICE
    // ================================
    private void addMultipleChoiceView(String label, List<String> choices) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_customize_multiple, layoutCustomizeContainer, false);

        TextView tvLabel = view.findViewById(R.id.tv_customize_label);
        LinearLayout layoutChoices = view.findViewById(R.id.rg_choices);
        tvLabel.setText(label);
        selectedOptions.put(label, new ArrayList<>());

        for (String choice : choices) {
            CheckBox cb = new CheckBox(this);
            cb.setText(choice);
            cb.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            cb.setTextSize(13);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                List<String> selected = selectedOptions.getOrDefault(label, new ArrayList<>());
                if (isChecked) selected.add(choice);
                else selected.remove(choice);
                selectedOptions.put(label, selected);
            });
            layoutChoices.addView(cb);
        }

        layoutCustomizeContainer.addView(view);
    }

    // ================================
    // CHECK WISHLIST
    // ================================
    private void checkWishlist() {
        if (uid == null) return;
        dbRef.child("wishlists").child(uid).child(foodId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        isWishlisted = snapshot.exists();
                        updateWishlistIcon();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void updateWishlistIcon() {
        if (isWishlisted) {
            btnWishlist.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnWishlist.setImageResource(R.drawable.ic_heart);
        }
        btnWishlist.setColorFilter(
                ContextCompat.getColor(this, android.R.color.holo_red_light));
    }

    private void toggleWishlist() {
        if (uid == null) {
            Toast.makeText(this, "Login dulu untuk wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference wishlistRef = dbRef.child("wishlists").child(uid).child(foodId);

        if (isWishlisted) {
            wishlistRef.removeValue().addOnSuccessListener(unused -> {
                isWishlisted = false;
                updateWishlistIcon();
                Toast.makeText(this, "Dihapus dari wishlist", Toast.LENGTH_SHORT).show();
            });
        } else {
            Map<String, Object> wishlistData = new HashMap<>();
            wishlistData.put("menuKey", foodId);
            wishlistData.put("name", foodName);
            wishlistData.put("price", foodPrice);
            wishlistData.put("imageBase64", foodImage);
            wishlistData.put("category", foodCategory);
            wishlistRef.setValue(wishlistData).addOnSuccessListener(unused -> {
                isWishlisted = true;
                updateWishlistIcon();
                Toast.makeText(this, "Ditambahkan ke wishlist", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ================================
    // CLICK LISTENERS
    // ================================
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnWishlist.setOnClickListener(v -> toggleWishlist());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    // ================================
    // ADD TO CART
    // ================================
    private void addToCart() {
        if (foodId == null || foodName == null) {
            Toast.makeText(this, "Data menu tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etNotes.getText().toString().trim();

        StringBuilder customizeStr = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                customizeStr.append(entry.getKey())
                        .append(": ")
                        .append(String.join(", ", entry.getValue()))
                        .append("\n");
            }
        }

        CartItem item = new CartItem(
                foodId,
                foodName,
                foodPrice,
                quantity,
                foodImage,
                customizeStr.toString().trim(),
                notes
        );

        CartManager.addItem(item);
        Toast.makeText(this, foodName + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        finish();
    }
}