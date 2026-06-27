package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private TextView tvSubtotal, tvShipping, tvTotal;
    private AppCompatButton btnConfirm;
    private CardView cardEwallet, cardCash;
    private RadioButton rbEwallet, rbCash;
    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // DATA
    // ================================
    private String selectedPayment = null;
    private double total = 0;
    private double subtotal = 0;
    private double shipping = 0;
    private String deliveryMethod = "Delivery";

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        getIntentData();
        setupPaymentOptions();
        setupBottomNav();
        setupListeners();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShipping = findViewById(R.id.tv_shipping);
        tvTotal = findViewById(R.id.tv_total);
        btnConfirm = findViewById(R.id.btn_confirm_checkout);
        cardEwallet = findViewById(R.id.card_ewallet);
        cardCash = findViewById(R.id.card_cash);
        rbEwallet = findViewById(R.id.rb_ewallet);
        rbCash = findViewById(R.id.rb_cash);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // GET INTENT DATA
    // ================================
    private void getIntentData() {
        subtotal = getIntent().getDoubleExtra("subtotal", 0);
        shipping = getIntent().getDoubleExtra("shipping", 0);
        total = getIntent().getDoubleExtra("total", 0);
        deliveryMethod = getIntent().getStringExtra("delivery_method") != null
                ? getIntent().getStringExtra("delivery_method") : "Delivery";

        tvSubtotal.setText(formatRupiah(subtotal));
        tvShipping.setText(shipping == 0 ? "Gratis" : formatRupiah(shipping));
        tvTotal.setText(formatRupiah(total));
    }

    // ================================
    // SETUP PAYMENT OPTIONS
    // ================================
    private void setupPaymentOptions() {
        // default tidak ada yang dipilih
        rbEwallet.setChecked(false);
        rbCash.setChecked(false);

        cardEwallet.setOnClickListener(v -> {
            selectedPayment = "E-Wallet";
            rbEwallet.setChecked(true);
            rbCash.setChecked(false);
            cardEwallet.setCardBackgroundColor(0xFFE8FFF4);
            cardCash.setCardBackgroundColor(0xFFFFFFFF);
        });

        cardCash.setOnClickListener(v -> {
            selectedPayment = "Cash";
            rbCash.setChecked(true);
            rbEwallet.setChecked(false);
            cardCash.setCardBackgroundColor(0xFFE8FFF4);
            cardEwallet.setCardBackgroundColor(0xFFFFFFFF);
        });

        rbEwallet.setOnClickListener(v -> cardEwallet.performClick());
        rbCash.setOnClickListener(v -> cardCash.performClick());
    }

    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> processCheckout());
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

        navCart.setOnClickListener(v ->
                Toast.makeText(this, "Anda sudah di Cart", Toast.LENGTH_SHORT).show()
        );

        navProfile.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });
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
    // PROCESS CHECKOUT
    // ================================
    private void processCheckout() {
        if (selectedPayment == null) {
            Toast.makeText(this,
                    "Pilih metode pembayaran dulu",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int rewardPoint = (int) (total / 10000);

        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        updatePoint(database, uid, rewardPoint);
        saveOrder(database, uid, rewardPoint);
    }

    // ================================
    // UPDATE POINT
    // ================================
    private void updatePoint(FirebaseDatabase database, String uid, int rewardPoint) {
        database.getReference("users").child(uid).child("point").get()
                .addOnSuccessListener(snapshot -> {
                    int oldPoint = 0;
                    if (snapshot.exists() && snapshot.getValue(Integer.class) != null) {
                        oldPoint = snapshot.getValue(Integer.class);
                    }
                    database.getReference("users").child(uid).child("point")
                            .setValue(oldPoint + rewardPoint);
                });
    }

    // ================================
    // SAVE ORDER
    // ================================
    private void saveOrder(FirebaseDatabase database, String uid, int rewardPoint) {

        DatabaseReference orderRef =
                database.getReference("orders")
                        .child(uid)
                        .push();

        String orderId = orderRef.getKey();

        String date =
                new SimpleDateFormat(
                        "dd MMM yyyy HH:mm",
                        Locale.getDefault()
                ).format(new Date());

        // ================================
        // BUILD ITEMS
        // ================================
        List<CartItem> cartItems =
                CartManager.getItems();

        HashMap<String, Object> itemsMap =
                new HashMap<>();

        for (CartItem item : cartItems) {

            HashMap<String, Object> itemData =
                    new HashMap<>();

            itemData.put("name", item.getName());
            itemData.put("price", item.getPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put(
                    "customizeOptions",
                    item.getCustomizeOptions()
            );

            itemData.put("notes", item.getNotes());

            itemData.put(
                    "imageBase64",
                    item.getImageBase64()
            );

            itemsMap.put(
                    item.getMenuKey(),
                    itemData
            );
        }

        // ================================
        // GET USER DATA
        // ================================
        DatabaseReference userRef =
                database.getReference("users")
                        .child(uid);

        userRef.get().addOnSuccessListener(userSnapshot -> {

            String customerName = "Unknown";
            String customerPhone = "Belum ada nomor";

            if (userSnapshot.exists()) {

                // NAME
                if (userSnapshot.child("name").getValue() != null) {

                    customerName =
                            userSnapshot.child("name")
                                    .getValue(String.class);
                }

                // PHONE
                if (userSnapshot.child("phone").getValue() != null) {

                    String phone =
                            userSnapshot.child("phone")
                                    .getValue(String.class);

                    if (phone != null && !phone.isEmpty()) {
                        customerPhone = phone;
                    }
                }
            }

            // ================================
            // ORDER MAP
            // ================================
            HashMap<String, Object> orderMap =
                    new HashMap<>();

            orderMap.put("orderId", orderId);

            orderMap.put(
                    "customerName",
                    customerName
            );

            orderMap.put(
                    "customerContact",
                    customerPhone
            );

            orderMap.put("total", total);
            orderMap.put("subtotal", subtotal);
            orderMap.put("shipping", shipping);

            orderMap.put(
                    "deliveryMethod",
                    deliveryMethod
            );

            orderMap.put(
                    "payment",
                    selectedPayment
            );

            orderMap.put("point", rewardPoint);

            orderMap.put("status", "Pending");

            orderMap.put("date", date);

            orderMap.put("items", itemsMap);

            // ================================
            // SAVE ORDER
            // ================================
            orderRef.setValue(orderMap)
                    .addOnSuccessListener(unused -> {

                        CartManager.clearCart();

                        Intent intent =
                                new Intent(
                                        this,
                                        PaymentSuccessActivity.class
                                );

                        intent.putExtra(
                                "orderId",
                                orderId
                        );

                        intent.putExtra(
                                "total",
                                total
                        );

                        intent.putExtra(
                                "payment",
                                selectedPayment
                        );

                        intent.putExtra(
                                "point",
                                rewardPoint
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                        );

                        startActivity(intent);

                        finish();
                    })
                    .addOnFailureListener(e ->

                            Toast.makeText(
                                    this,
                                    "Gagal menyimpan order: "
                                            + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }

    private String formatRupiah(double value) {
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value);
    }
}