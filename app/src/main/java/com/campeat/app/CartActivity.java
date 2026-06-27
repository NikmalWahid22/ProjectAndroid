package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.CartAdapter;
import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.graphics.Color;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;

import com.airbnb.lottie.LottieAnimationView;

public class CartActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyCart;
    private AppCompatButton btnCheckout;
    private AppCompatButton btnBackToHome;
    private ImageView btnOrderHistory;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private LottieAnimationView lottieEmptyCart;
    private MaterialCardView cardDelivery, cardPickup;
    private ImageView iconDelivery, iconPickup;
    private TextView textDeliveryTitle, textDeliveryPrice, textPickupTitle, textPickupPrice;

    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // DATA
    // ================================
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private boolean isDelivery = true;

    private static final double SHIPPING_COST_DELIVERY = 5000;
    private static final double SHIPPING_COST_PICKUP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        loadCartData();
        checkCartStatus();
        setupDeliveryOptions();
        setupBottomNav();
        setupListeners();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_cart_items);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
        btnOrderHistory = findViewById(R.id.btn_order_history);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShipping = findViewById(R.id.tv_shipping);
        iconDelivery = findViewById(R.id.icon_delivery);
        iconPickup = findViewById(R.id.icon_pickup);
        textDeliveryTitle = findViewById(R.id.text_delivery_title);
        textDeliveryPrice = findViewById(R.id.text_delivery_price);
        textPickupTitle = findViewById(R.id.text_pickup_title);
        textPickupPrice = findViewById(R.id.text_pickup_price);
        tvTotal = findViewById(R.id.tv_total);
        cardDelivery = findViewById(R.id.card_delivery);
        cardPickup = findViewById(R.id.card_pickup);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);

        lottieEmptyCart = findViewById(R.id.lottie_empty_cart);
    }

    // ================================
    // LOAD CART DATA
    // ================================
    private void loadCartData() {
        cartItemList = CartManager.getItems();
    }

    // ================================
// CEK STATUS CART
// ================================
    private void checkCartStatus() {
        if (cartItemList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
            findViewById(R.id.tv_delivery_title).setVisibility(View.GONE);
            findViewById(R.id.layout_delivery_options).setVisibility(View.GONE);
            findViewById(R.id.card_price_summary).setVisibility(View.GONE);

            if (lottieEmptyCart != null) {
                lottieEmptyCart.playAnimation();
            }
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_delivery_title).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_delivery_options).setVisibility(View.VISIBLE);
            findViewById(R.id.card_price_summary).setVisibility(View.VISIBLE);

            if (lottieEmptyCart != null) {
                lottieEmptyCart.cancelAnimation();
            }

            setupRecyclerView();
            updatePriceSummary();
        }
    }


    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(cartAdapter);
        cartAdapter.setOnQuantityChangeListener(() -> {
            updatePriceSummary();
            if (cartItemList.isEmpty()) checkCartStatus();
        });
    }

    // ================================
    // SETUP DELIVERY OPTIONS
    // ================================
    private void setupDeliveryOptions() {
        selectDelivery();

        cardDelivery.setOnClickListener(v -> {
            isDelivery = true;
            selectDelivery();
            updatePriceSummary();
        });

        cardPickup.setOnClickListener(v -> {
            isDelivery = false;
            selectPickup();
            updatePriceSummary();
        });
    }

    private void selectDelivery() {
        cardDelivery.setCardBackgroundColor(Color.parseColor("#006C46"));
        cardPickup.setCardBackgroundColor(Color.WHITE);

        ImageViewCompat.setImageTintList(iconDelivery, ColorStateList.valueOf(Color.WHITE));
        textDeliveryTitle.setTextColor(Color.WHITE);
        textDeliveryPrice.setTextColor(Color.parseColor("#87FFD0"));

        ImageViewCompat.setImageTintList(iconPickup, ColorStateList.valueOf(Color.parseColor("#006C46")));
        textPickupTitle.setTextColor(Color.parseColor("#002112"));
        textPickupPrice.setTextColor(Color.parseColor("#006C46"));

        animateOption(cardDelivery);
    }

    private void selectPickup() {
        cardPickup.setCardBackgroundColor(Color.parseColor("#006C46"));
        cardDelivery.setCardBackgroundColor(Color.WHITE);

        ImageViewCompat.setImageTintList(iconPickup, ColorStateList.valueOf(Color.WHITE));
        textPickupTitle.setTextColor(Color.WHITE);
        textPickupPrice.setTextColor(Color.parseColor("#87FFD0"));

        ImageViewCompat.setImageTintList(iconDelivery, ColorStateList.valueOf(Color.parseColor("#006C46")));
        textDeliveryTitle.setTextColor(Color.parseColor("#002112"));
        textDeliveryPrice.setTextColor(Color.parseColor("#006C46"));

        animateOption(cardPickup);
    }

    private void animateOption(View view) {
        view.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(90)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }



    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {

        btnBackToHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        btnOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class))
        );

        btnCheckout.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Cart masih kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            double subtotal = calculateSubtotal();
            double shipping = isDelivery ? SHIPPING_COST_DELIVERY : SHIPPING_COST_PICKUP;
            double total = subtotal + shipping;
            String deliveryMethod = isDelivery ? "Delivery" : "Pickup";

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("total", total);
            intent.putExtra("subtotal", subtotal);
            intent.putExtra("shipping", shipping);
            intent.putExtra("delivery_method", deliveryMethod);
            startActivity(intent);
        });
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
    // PRICE CALCULATION
    // ================================
    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItemList) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    private void updatePriceSummary() {
        double subtotal = calculateSubtotal();
        double shipping = isDelivery ? SHIPPING_COST_DELIVERY : SHIPPING_COST_PICKUP;
        double total = subtotal + shipping;

        tvSubtotal.setText(formatRupiah(subtotal));
        tvShipping.setText(shipping == 0 ? "Gratis" : formatRupiah(shipping));
        tvTotal.setText(formatRupiah(total));
        btnCheckout.setText("Checkout " + formatRupiah(total));
    }

    private String formatRupiah(double value) {
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value);
    }

    // ================================
    // REFRESH SAAT BALIK KE CART
    // ================================
    @Override
    protected void onResume() {
        super.onResume();
        loadCartData();
        checkCartStatus();
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
            updatePriceSummary();
        }
    }
}