package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.CartAdapter;
import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View layoutEmptyCart, layoutContentCart;
    private Button btnCheckout;
    private CheckBox checkboxDeliver, checkboxPickup;
    private TextView textSubtotalValue, textDiscountValue, textShippingValue, textTotalValue;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private static final double DISCOUNT_AMOUNT = 10000;
    private static final double SHIPPING_COST_DELIVERY = 5000;
    private static final double SHIPPING_COST_PICKUP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadCartData();
        checkCartStatus();

        if (!cartItemList.isEmpty()) {
            setupRecyclerView();
            updatePriceSummary();
        }

        setupListeners();
        setupBottomNav();
    }

    // =========================================================================
    // INIT VIEWS
    // =========================================================================

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_cart_items);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        layoutContentCart = findViewById(R.id.layout_content_cart);
        btnCheckout = findViewById(R.id.btn_checkout);

        checkboxDeliver = findViewById(R.id.checkbox_deliver);
        checkboxPickup = findViewById(R.id.checkbox_pickup);

        View subtotalRow = findViewById(R.id.price_row_subtotal);
        View discountRow = findViewById(R.id.price_row_discount);
        View shippingRow = findViewById(R.id.price_row_shipping);
        View totalRow = findViewById(R.id.price_row_total);

        textSubtotalValue = subtotalRow.findViewById(R.id.text_row_value);
        textDiscountValue = discountRow.findViewById(R.id.text_row_value);
        textShippingValue = shippingRow.findViewById(R.id.text_row_value);
        textTotalValue = totalRow.findViewById(R.id.text_total_value);
    }

    // =========================================================================
    // DATA
    // =========================================================================

    private void loadCartData() {
        cartItemList = CartManager.getCart();
    }

    private void checkCartStatus() {
        if (cartItemList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutContentCart.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            layoutContentCart.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        cartAdapter.setOnQuantityChangeListener(this::updatePriceSummary);
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private void setupListeners() {

        btnCheckout.setOnClickListener(v -> {

            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Cart masih kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            double subtotal = calculateSubtotal();
            double shipping = getShippingCost();
            double total = subtotal - DISCOUNT_AMOUNT + shipping;
            if (total < 0) total = 0;

            String deliveryMethod = checkboxDeliver.isChecked() ? "Delivery" : "Pickup";

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("total", total);
            intent.putExtra("subtotal", subtotal);
            intent.putExtra("discount", DISCOUNT_AMOUNT);
            intent.putExtra("shipping", shipping);
            intent.putExtra("total", total);
            intent.putExtra("delivery_method", deliveryMethod);

            startActivity(intent);
        });

        checkboxDeliver.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) checkboxPickup.setChecked(false);
            updatePriceSummary();
        });

        checkboxPickup.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) checkboxDeliver.setChecked(false);
            updatePriceSummary();
        });

        if (!checkboxDeliver.isChecked() && !checkboxPickup.isChecked()) {
            checkboxDeliver.setChecked(true);
        }
    }


    // =========================================================================
    // PRICE CALCULATION
    // =========================================================================

    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItemList) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    private double getShippingCost() {
        return checkboxDeliver.isChecked()
                ? SHIPPING_COST_DELIVERY
                : SHIPPING_COST_PICKUP;
    }

    private void updatePriceSummary() {
        double subtotal = calculateSubtotal();
        double shipping = getShippingCost();
        double total = subtotal - DISCOUNT_AMOUNT + shipping;
        if (total < 0) total = 0;

        textSubtotalValue.setText(formatRupiah(subtotal));
        textDiscountValue.setText("- " + formatRupiah(DISCOUNT_AMOUNT));
        textShippingValue.setText(formatRupiah(shipping));
        textTotalValue.setText(formatRupiah(total));

        btnCheckout.setText("Checkout " + formatRupiah(total));
    }

    private String formatRupiah(double value) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value).replace("Rp", "Rp ").trim();
    }

    // =========================================================================
    // BOTTOM NAVIGATION
    // =========================================================================

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_bar);
        bottomNav.setSelectedItemId(R.id.nav_cart);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_cart) return true;

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    // =========================================================================
    // REFRESH SAAT BALIK KE CART
    // =========================================================================

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
