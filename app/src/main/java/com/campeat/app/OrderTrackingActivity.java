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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // Disimpan setelah load dari Firebase untuk dikirim ke Receipt
    private double totalFromFirebase = 0;
    private int    pointFromFirebase  = 0;
    private String paymentFromFirebase = "";

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getIntentData();
        initViews();
        loadOrderFromFirebase();
        setupBottomNav();

        btnBack.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(this::finish, 100);
        });
    }

    // ================================
    // GET INTENT DATA
    // ================================
    private void getIntentData() {
        orderId = getIntent().getStringExtra("orderId");
        total   = getIntent().getDoubleExtra("total", 0);
        point   = getIntent().getIntExtra("point", 0);
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        btnBack         = findViewById(R.id.btn_back);
        tvQueueNumber   = findViewById(R.id.tv_queue_number);
        tvEstimatedTime = findViewById(R.id.tv_estimated_time);
        tvStatusTitle   = findViewById(R.id.tv_status_title);
        tvStatusDesc    = findViewById(R.id.tv_status_desc);
        tvYourPoint     = findViewById(R.id.tv_your_point);
        tvTotal         = findViewById(R.id.tv_total);
        icStep1         = findViewById(R.id.ic_step1);
        icStep2         = findViewById(R.id.ic_step2);
        icStep3         = findViewById(R.id.ic_step3);
        icStep4         = findViewById(R.id.ic_step4);
        tvStep1Time     = findViewById(R.id.tv_step1_time);
        rvOrderItems    = findViewById(R.id.rv_order_items);
        navHome         = findViewById(R.id.nav_home);
        navSearch       = findViewById(R.id.nav_search);
        navCart         = findViewById(R.id.nav_cart);
        navProfile      = findViewById(R.id.nav_profile);
    }

    // ================================
    // LOAD ORDER
    // ================================
    private void loadOrderFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

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

                        // STATUS
                        String status = snapshot.child("status").getValue(String.class);
                        String estimatedTime = "15 mins";

                        if (status != null) {
                            switch (status) {
                                case "Pending":   estimatedTime = "15 mins";   break;
                                case "Process":   estimatedTime = "10 mins";   break;
                                case "Ready":     estimatedTime = "2 mins";    break;
                                case "Done":      estimatedTime = "Completed"; break;
                            }
                        }

                        tvEstimatedTime.setText(estimatedTime);
                        updateStatusUI(status != null ? status : "Pending");

                        // DATE
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) tvStep1Time.setText(date + " · Success");

                        // POINT
                        Integer pointVal = snapshot.child("point").getValue(Integer.class);
                        pointFromFirebase = pointVal != null ? pointVal : 0;
                        tvYourPoint.setText("⭐ +" + pointFromFirebase + " pt  · View Receipt →");
                        tvYourPoint.setClickable(true);
                        tvYourPoint.setFocusable(true);

                        // TOTAL
                        Double totalVal = snapshot.child("total").getValue(Double.class);
                        totalFromFirebase = totalVal != null ? totalVal : 0;
                        Locale localeID = new Locale("id", "ID");
                        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
                        tvTotal.setText(format.format(totalFromFirebase));

                        // PAYMENT
                        String paymentVal = snapshot.child("payment").getValue(String.class);
                        paymentFromFirebase = paymentVal != null ? paymentVal : "";

                        // VIEW RECEIPT CLICK — setelah data loaded
                        tvYourPoint.setOnClickListener(v -> {
                            Intent intent = new Intent(
                                    OrderTrackingActivity.this,
                                    ReceiptActivity.class
                            );
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("total",   totalFromFirebase);
                            intent.putExtra("payment", paymentFromFirebase);
                            intent.putExtra("point",   pointFromFirebase);
                            startActivity(intent);
                        });

                        // ITEMS
                        List<TrackingItem> items = new ArrayList<>();
                        for (DataSnapshot itemSnap : snapshot.child("items").getChildren()) {
                            String name        = itemSnap.child("name").getValue(String.class);
                            Double price       = itemSnap.child("price").getValue(Double.class);
                            Integer qty        = itemSnap.child("quantity").getValue(Integer.class);
                            String customize   = itemSnap.child("customizeOptions").getValue(String.class);
                            String notes       = itemSnap.child("notes").getValue(String.class);
                            String imageBase64 = itemSnap.child("imageBase64").getValue(String.class);

                            if (name != null) {
                                items.add(new TrackingItem(
                                        name,
                                        price    != null ? price  : 0,
                                        qty      != null ? qty    : 1,
                                        customize, notes, imageBase64
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
                tvStatusDesc.setText("Pesananmu telah diambil. Selamat menikmati!");
                icStep1.setImageResource(R.drawable.ic_step_done);
                icStep2.setImageResource(R.drawable.ic_step_done);
                icStep3.setImageResource(R.drawable.ic_step_done);
                icStep4.setImageResource(R.drawable.ic_step_done);
                break;
        }
    }

    // ================================
    // BOTTOM NAVIGATION
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
                .scaleX(0.96f).scaleY(0.96f).alpha(0.82f).setDuration(70)
                .withEndAction(() -> view.animate()
                        .scaleX(1f).scaleY(1f).alpha(1f).setDuration(130)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.4f))
                        .start())
                .start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
    }

    // ================================
    // MODEL ITEM
    // ================================
    private static class TrackingItem {
        String name, customize, notes, imageBase64;
        double price;
        int qty;

        TrackingItem(String name, double price, int qty,
                     String customize, String notes, String imageBase64) {
            this.name        = name;
            this.price       = price;
            this.qty         = qty;
            this.customize   = customize;
            this.notes       = notes;
            this.imageBase64 = imageBase64;
        }
    }

    // ================================
    // ADAPTER
    // ================================
    private class TrackingItemAdapter
            extends RecyclerView.Adapter<TrackingItemAdapter.ViewHolder> {

        private final List<TrackingItem> items;

        TrackingItemAdapter(List<TrackingItem> items) { this.items = items; }

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

            if (item.customize != null && !item.customize.trim().isEmpty()) {
                holder.tvCustomize.setVisibility(View.VISIBLE);
                holder.tvCustomize.setText(item.customize);
            } else {
                holder.tvCustomize.setVisibility(View.GONE);
            }

            if (item.notes != null && !item.notes.trim().isEmpty()) {
                holder.tvNotes.setVisibility(View.VISIBLE);
                holder.tvNotes.setText("Catatan: " + item.notes);
            } else {
                holder.tvNotes.setVisibility(View.GONE);
            }

            Locale localeID = new Locale("id", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            holder.tvPrice.setText(format.format(item.price * item.qty));

            if (item.imageBase64 != null && !item.imageBase64.isEmpty()) {
                byte[] decodedBytes = Base64.decode(item.imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgItem.setImageBitmap(bitmap);
            } else {
                holder.imgItem.setImageResource(R.drawable.img_placeholder);
            }
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView imgItem;
            TextView tvName, tvCustomize, tvNotes, tvPrice;

            ViewHolder(View itemView) {
                super(itemView);
                imgItem     = itemView.findViewById(R.id.img_order_item);
                tvName      = itemView.findViewById(R.id.tv_order_item_name);
                tvCustomize = itemView.findViewById(R.id.tv_order_item_customize);
                tvNotes     = itemView.findViewById(R.id.tv_item_notes);
                tvPrice     = itemView.findViewById(R.id.tv_order_item_price);
            }
        }
    }
}