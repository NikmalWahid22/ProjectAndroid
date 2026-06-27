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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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

public class ReceiptActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private ImageView btnBack;
    private TextView tvOrderId, tvDate, tvPayment, tvStatus;
    private TextView tvPoint, tvSubtotal, tvShipping, tvTotal;
    private RecyclerView rvItems;
    private AppCompatButton btnHome;

    // ================================
    // DATA
    // ================================
    private String orderId;
    private double total;
    private String payment;
    private int point;

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        getIntentData();
        initViews();
        loadOrderFromFirebase();
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
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvOrderId = findViewById(R.id.tv_receipt_order_id);
        tvDate = findViewById(R.id.tv_receipt_date);
        tvPayment = findViewById(R.id.tv_receipt_payment);
        tvStatus = findViewById(R.id.tv_receipt_status);
        tvPoint = findViewById(R.id.tv_receipt_point);
        tvSubtotal = findViewById(R.id.tv_receipt_subtotal);
        tvShipping = findViewById(R.id.tv_receipt_shipping);
        tvTotal = findViewById(R.id.tv_receipt_total);
        rvItems = findViewById(R.id.rv_receipt_items);
        btnHome = findViewById(R.id.btn_home);
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // ORDER ID
                        String shortId = orderId.length() > 6
                                ? orderId.substring(orderId.length() - 6).toUpperCase()
                                : orderId.toUpperCase();
                        tvOrderId.setText("#CE-" + shortId);

                        // DATE
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) tvDate.setText(date);

                        // PAYMENT
                        String paymentVal = snapshot.child("payment").getValue(String.class);
                        if (paymentVal != null) tvPayment.setText(paymentVal);

                        // STATUS
                        String status = snapshot.child("status").getValue(String.class);
                        if (status != null) tvStatus.setText(status);

                        // POINT
                        Integer pointVal = snapshot.child("point").getValue(Integer.class);
                        tvPoint.setText("+" + (pointVal != null ? pointVal : 0) + " poin");

                        // SUBTOTAL & SHIPPING
                        Double subtotalVal = snapshot.child("subtotal").getValue(Double.class);
                        Double shippingVal = snapshot.child("shipping").getValue(Double.class);
                        Double totalVal = snapshot.child("total").getValue(Double.class);

                        tvSubtotal.setText(formatRupiah(subtotalVal != null ? subtotalVal : 0));
                        tvShipping.setText(shippingVal != null && shippingVal == 0
                                ? "Gratis" : formatRupiah(shippingVal != null ? shippingVal : 0));
                        tvTotal.setText(formatRupiah(totalVal != null ? totalVal : 0));

                        // ITEMS
                        List<ReceiptItem> items = new ArrayList<>();
                        DataSnapshot itemsSnap = snapshot.child("items");
                        for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                            String name = itemSnap.child("name").getValue(String.class);
                            Double price = itemSnap.child("price").getValue(Double.class);
                            Integer qty = itemSnap.child("quantity").getValue(Integer.class);
                            String customize = itemSnap.child("customizeOptions").getValue(String.class);

                            if (name != null) {
                                items.add(new ReceiptItem(
                                        name,
                                        price != null ? price : 0,
                                        qty != null ? qty : 1,
                                        customize
                                ));
                            }
                        }

                        rvItems.setLayoutManager(new LinearLayoutManager(ReceiptActivity.this));
                        rvItems.setNestedScrollingEnabled(false);
                        rvItems.setAdapter(new ReceiptItemAdapter(items));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================================
    // SETUP LISTENERS
    // ================================
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String formatRupiah(double value) {
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(value);
    }

    // ================================
    // MODEL RECEIPT ITEM
    // ================================
    private static class ReceiptItem {
        String name;
        double price;
        int qty;
        String customize;

        ReceiptItem(String name, double price, int qty, String customize) {
            this.name = name;
            this.price = price;
            this.qty = qty;
            this.customize = customize;
        }
    }

    // ================================
    // ADAPTER RECEIPT ITEM
    // ================================
    private class ReceiptItemAdapter
            extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {

        private List<ReceiptItem> items;

        ReceiptItemAdapter(List<ReceiptItem> items) {
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
            ReceiptItem item = items.get(position);

            holder.tvName.setText(item.name + " x" + item.qty);
            holder.tvCustomize.setText(item.customize != null ? item.customize : "");
            holder.tvCustomize.setVisibility(
                    item.customize != null && !item.customize.isEmpty()
                            ? View.VISIBLE : View.GONE);

            Locale localeID = new Locale("id", "ID");
            NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
            holder.tvPrice.setText(format.format(item.price * item.qty));

            // no image di receipt
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