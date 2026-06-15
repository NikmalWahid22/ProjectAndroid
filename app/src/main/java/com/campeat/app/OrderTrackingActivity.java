package com.campeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderTrackingActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private ImageView btnBack;
    private TextView tvQueueNumber, tvEstimatedTime;
    private TextView tvStatusTitle, tvStatusDesc;
    private TextView tvYourPoint, tvTotal;
    private ImageView icStep1, icStep2, icStep3, icStep4;
    private TextView tvStep1Time;
    private RecyclerView rvOrderItems;
    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // DATA
    // ================================
    private String orderId;
    private double total;
    private int point;

    private final String DB_URL =
            "https://campeat-8c587-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        getIntentData();
        initViews();
        loadOrderFromFirebase();
        setupBottomNav();

        btnBack.setOnClickListener(v -> finish());
    }

    // ================================
    // GET INTENT DATA
    // ================================
    private void getIntentData() {
        orderId = getIntent().getStringExtra("orderId");
        total = getIntent().getDoubleExtra("total", 0);
        point = getIntent().getIntExtra("point", 0);
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvQueueNumber = findViewById(R.id.tv_queue_number);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        tvStatusDesc = findViewById(R.id.tv_status_desc);
        tvYourPoint = findViewById(R.id.tv_your_point);
        tvTotal = findViewById(R.id.tv_total);
        icStep1 = findViewById(R.id.ic_step1);
        icStep2 = findViewById(R.id.ic_step2);
        icStep3 = findViewById(R.id.ic_step3);
        icStep4 = findViewById(R.id.ic_step4);
        tvStep1Time = findViewById(R.id.tv_step1_time);
        rvOrderItems = findViewById(R.id.rv_order_items);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // LOAD ORDER FROM FIREBASE
    // ================================
    private void loadOrderFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null || orderId == null) return;

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("orders")
                .child(uid)
                .child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        // QUEUE NUMBER
                        String shortId = orderId.length() > 6
                                ? orderId.substring(orderId.length() - 6).toUpperCase()
                                : orderId.toUpperCase();
                        tvQueueNumber.setText("#CE-" + shortId);

                        // ESTIMATED TIME dari Firebase
                        String estimatedTime = snapshot.child("estimatedTime")
                                .getValue(String.class);
                        tvEstimatedTime.setText(
                                estimatedTime != null ? estimatedTime : "Menunggu...");

                        // STATUS
                        String status = snapshot.child("status").getValue(String.class);
                        updateStatusUI(status != null ? status : "Pending");

                        // DATE
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) tvStep1Time.setText(date + " · Success");

                        // POINT
                        Integer pointVal = snapshot.child("point").getValue(Integer.class);
                        tvYourPoint.setText("⭐ +" + (pointVal != null ? pointVal : 0) + " pt");

                        // TOTAL
                        Double totalVal = snapshot.child("total").getValue(Double.class);
                        Locale localeID = new Locale("id", "ID");
                        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
                        tvTotal.setText(format.format(totalVal != null ? totalVal : 0));

                        // ITEMS
                        List<TrackingItem> items = new ArrayList<>();
                        DataSnapshot itemsSnap = snapshot.child("items");
                        for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                            String name = itemSnap.child("name").getValue(String.class);
                            Double price = itemSnap.child("price").getValue(Double.class);
                            Integer qty = itemSnap.child("quantity").getValue(Integer.class);
                            String customize = itemSnap.child("customizeOptions")
                                    .getValue(String.class);

                            if (name != null) {
                                items.add(new TrackingItem(
                                        name,
                                        price != null ? price : 0,
                                        qty != null ? qty : 1,
                                        customize
                                ));
                            }
                        }

                        rvOrderItems.setLayoutManager(
                                new LinearLayoutManager(OrderTrackingActivity.this));
                        rvOrderItems.setNestedScrollingEnabled(false);
                        rvOrderItems.setAdapter(new TrackingItemAdapter(items));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(OrderTrackingActivity.this,
                                "Gagal load order", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================================
    // UPDATE STATUS UI
    // ================================
    private void updateStatusUI(String status) {
        switch (status) {
            case "Pending":
                tvStatusTitle.setText("Order Confirmed");
                tvStatusDesc.setText("Pesanan kamu telah diterima dan sedang menunggu diproses.");
                icStep1.setImageResource(R.drawable.ic_step_done);
                icStep2.setImageResource(R.drawable.ic_step_inactive);
                icStep3.setImageResource(R.drawable.ic_step_inactive);
                icStep4.setImageResource(R.drawable.ic_step_inactive);
                break;

            case "Process":
                tvStatusTitle.setText("Preparing Your Meal");
                tvStatusDesc.setText("Dapur kami sedang menyiapkan pesananmu dengan bahan-bahan segar.");
                icStep1.setImageResource(R.drawable.ic_step_done);
                icStep2.setImageResource(R.drawable.ic_step_active);
                icStep3.setImageResource(R.drawable.ic_step_inactive);
                icStep4.setImageResource(R.drawable.ic_step_inactive);
                break;

            case "Ready":
                tvStatusTitle.setText("Ready for Pickup!");
                tvStatusDesc.setText("Pesananmu sudah siap! Silakan ambil di kantin.");
                icStep1.setImageResource(R.drawable.ic_step_done);
                icStep2.setImageResource(R.drawable.ic_step_done);
                icStep3.setImageResource(R.drawable.ic_step_active);
                icStep4.setImageResource(R.drawable.ic_step_inactive);
                break;

            case "Done":
                tvStatusTitle.setText("Order Complete!");
                tvStatusDesc.setText("Pesananmu telah diambil. Selamat menikmati! 😊");
                icStep1.setImageResource(R.drawable.ic_step_done);
                icStep2.setImageResource(R.drawable.ic_step_done);
                icStep3.setImageResource(R.drawable.ic_step_done);
                icStep4.setImageResource(R.drawable.ic_step_done);
                break;

            default:
                tvStatusTitle.setText("Order Confirmed");
                tvStatusDesc.setText("Pesanan kamu telah diterima.");
                break;
        }
    }

    // ================================
    // SETUP BOTTOM NAV
    // ================================
    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            finish();
        });

        navCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    // ================================
    // MODEL TRACKING ITEM
    // ================================
    private static class TrackingItem {
        String name;
        double price;
        int qty;
        String customize;

        TrackingItem(String name, double price, int qty, String customize) {
            this.name = name;
            this.price = price;
            this.qty = qty;
            this.customize = customize;
        }
    }

    // ================================
    // ADAPTER TRACKING ITEM
    // ================================
    private class TrackingItemAdapter
            extends RecyclerView.Adapter<TrackingItemAdapter.ViewHolder> {

        private List<TrackingItem> items;

        TrackingItemAdapter(List<TrackingItem> items) {
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
            TrackingItem item = items.get(position);

            holder.tvName.setText(item.name + " x" + item.qty);
            holder.tvCustomize.setText(
                    item.customize != null ? item.customize : "");
            holder.tvCustomize.setVisibility(
                    item.customize != null && !item.customize.isEmpty()
                            ? View.VISIBLE : View.GONE);

            Locale localeID = new Locale("id", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            holder.tvPrice.setText(format.format(item.price * item.qty));

            holder.imgItem.setImageResource(R.drawable.img_placeholder);
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView imgItem;
            TextView tvName, tvCustomize, tvPrice;

            ViewHolder(View itemView) {
                super(itemView);
                imgItem = itemView.findViewById(R.id.img_order_item);
                tvName = itemView.findViewById(R.id.tv_order_item_name);
                tvCustomize = itemView.findViewById(R.id.tv_order_item_customize);
                tvPrice = itemView.findViewById(R.id.tv_order_item_price);
            }
        }
    }
}