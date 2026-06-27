package com.campeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PaymentSuccessActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private TextView tvOrderId, tvOrderDate, tvTotal, tvViewReceipt;
    private AppCompatButton btnLihatPesanan, btnHome;
    private RecyclerView rvOrderItems;
    private LinearLayout navHome, navSearch, navCart, navProfile;
    private LottieAnimationView imgSuccess;

    // ================================
    // DATA
    // ================================
    private String orderId;
    private double total;
    private String payment;
    private int point;
    private List<CartItem> orderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getIntentData();
        initViews();

        if (imgSuccess != null) {
            imgSuccess.playAnimation();
        }

        loadData();
        setupRecyclerView();
        setupBottomNav();
        setupListeners();
    }

    // ================================
    // GET INTENT DATA
    // ================================
    private void getIntentData() {
        orderId = getIntent().getStringExtra("orderId");
        total = getIntent().getDoubleExtra("total", 0);
        payment = getIntent().getStringExtra("payment");
        point = getIntent().getIntExtra("point", 0);
        orderItems = CartManager.getItems();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvTotal = findViewById(R.id.tv_total);
        tvViewReceipt = findViewById(R.id.tv_view_receipt);
        btnLihatPesanan = findViewById(R.id.btn_lihat_pesanan);
        btnHome = findViewById(R.id.btn_home);
        rvOrderItems = findViewById(R.id.rv_order_items);
        imgSuccess = findViewById(R.id.img_success);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // LOAD DATA
    // ================================
    private void loadData() {
        if (orderId != null) {
            String shortId = orderId.length() > 6
                    ? orderId.substring(orderId.length() - 6).toUpperCase()
                    : orderId.toUpperCase();

            tvOrderId.setText("#CE-" + shortId);
            tvOrderDate.setText("Transaction ID: " + shortId);
        }

        tvTotal.setText(formatRupiah(total));
    }

    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setNestedScrollingEnabled(false);
        rvOrderItems.setAdapter(new OrderItemAdapter(orderItems));
    }

    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {
        tvViewReceipt.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(this, ReceiptActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("total", total);
                intent.putExtra("payment", payment);
                intent.putExtra("point", point);
                startActivity(intent);
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
            }, 100);
        });

        btnLihatPesanan.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(
                        PaymentSuccessActivity.this,
                        OrderTrackingActivity.class
                );

                intent.putExtra("orderId", orderId);
                intent.putExtra("total", total);
                intent.putExtra("point", point);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });

        btnHome.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
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

        navCart.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
                finish();
            }, 100);
        });

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

    private String formatRupiah(double value) {
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value);
    }

    // ================================
    // INNER ADAPTER UNTUK ORDER ITEMS
    // ================================
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

        private List<CartItem> items;

        public OrderItemAdapter(List<CartItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem item = items.get(position);

            holder.tvName.setText(item.getName());

            holder.tvCustomize.setText(
                    item.getCustomizeOptions() != null && !item.getCustomizeOptions().isEmpty()
                            ? item.getCustomizeOptions()
                            : ""
            );

            holder.tvCustomize.setVisibility(
                    item.getCustomizeOptions() != null && !item.getCustomizeOptions().isEmpty()
                            ? View.VISIBLE
                            : View.GONE
            );

            Locale localeID = new Locale("id", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            holder.tvPrice.setText(format.format(item.getPrice() * item.getQuantity()));

            if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
                byte[] decoded = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.imgItem.setImageBitmap(bitmap);
            } else {
                holder.imgItem.setImageResource(R.drawable.img_placeholder);
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView imgItem;
            TextView tvName, tvCustomize, tvPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                imgItem = itemView.findViewById(R.id.img_order_item);
                tvName = itemView.findViewById(R.id.tv_order_item_name);
                tvCustomize = itemView.findViewById(R.id.tv_order_item_customize);
                tvPrice = itemView.findViewById(R.id.tv_order_item_price);
            }
        }
    }
}