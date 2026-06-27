package com.campeat.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.OrderItemAdapter;
import com.campeat.app.model.OrderItem;
import com.campeat.app.model.OrderItemFirebase;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdminActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvPlacedOn, tvOrderStatus;
    private TextView tvCustomerName, tvCustomerContact, tvPaymentMethod, tvPickupMethod;

    private MaterialCardView btnStatusPending;
    private MaterialCardView btnStatusProcess;
    private MaterialCardView btnStatusReady;
    private MaterialCardView btnStatusSuccess;

    private AppCompatButton btnSaveChanges, btnCancel;
    private RecyclerView rvOrderItems;

    private List<OrderItem> itemList;
    private OrderItemAdapter adapter;

    private DatabaseReference ordersRef;
    private DatabaseReference usersRef;

    private String selectedStatus = "Pending";
    private String orderId = "";
    private String uid = "";

    private static final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_admin);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        getIntentData();
        setupButtons();

        loadOrderDetail();
        loadUserDetail();
        loadOrderItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);

        tvPlacedOn = findViewById(R.id.tv_placed_on);
        tvOrderStatus = findViewById(R.id.tv_order_status);

        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerContact = findViewById(R.id.tv_customer_contact);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvPickupMethod = findViewById(R.id.tv_pickup_method);

        btnStatusPending = findViewById(R.id.btn_status_pending);
        btnStatusProcess = findViewById(R.id.btn_status_process);
        btnStatusReady = findViewById(R.id.btn_status_ready);
        btnStatusSuccess = findViewById(R.id.btn_status_success);

        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnCancel = findViewById(R.id.btn_cancel);

        rvOrderItems = findViewById(R.id.recycler_order_items);

        itemList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        ordersRef = database.getReference("orders");
        usersRef = database.getReference("users");
    }

    private void setupRecyclerView() {
        adapter = new OrderItemAdapter(this, itemList);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(adapter);
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void getIntentData() {
        if (getIntent() == null) return;

        orderId = getIntent().getStringExtra("orderId");
        uid = getIntent().getStringExtra("uid");

        if (orderId == null) orderId = "";
        if (uid == null) uid = "";
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnStatusPending.setOnClickListener(v -> selectStatus("Pending", true));
        btnStatusProcess.setOnClickListener(v -> selectStatus("Process", true));
        btnStatusReady.setOnClickListener(v -> selectStatus("Ready", true));
        btnStatusSuccess.setOnClickListener(v -> selectStatus("Done", true));

        btnSaveChanges.setOnClickListener(v -> saveStatus());
    }

    private void loadOrderDetail() {
        if (uid.isEmpty() || orderId.isEmpty()) {
            Toast.makeText(this, "UID atau Order ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        ordersRef.child(uid).child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(OrderDetailAdminActivity.this,
                                    "Data order tidak ditemukan",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String date = getString(snapshot, "date", "-");
                        String status = getString(snapshot, "status", "Pending");
                        String payment = getString(snapshot, "payment", "-");

                        String pickupMethod = getString(snapshot, "pickupMethod", "");
                        if (pickupMethod.isEmpty()) pickupMethod = getString(snapshot, "deliveryMethod", "");
                        if (pickupMethod.isEmpty()) pickupMethod = getString(snapshot, "delivery_method", "");
                        if (pickupMethod.isEmpty()) pickupMethod = "Pickup";

                        String customerName = getString(snapshot, "customerName", "");
                        String customerContact = getString(snapshot, "customerContact", "");

                        tvPlacedOn.setText(date);
                        tvOrderStatus.setText(status);
                        tvPaymentMethod.setText(payment);
                        tvPickupMethod.setText(pickupMethod);

                        if (!customerName.isEmpty()) tvCustomerName.setText(customerName);
                        if (!customerContact.isEmpty()) tvCustomerContact.setText(customerContact);

                        selectedStatus = status;
                        selectStatus(status, false);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(OrderDetailAdminActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserDetail() {
        if (uid.isEmpty()) return;

        usersRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        String name = getString(snapshot, "name", "");
                        if (name.isEmpty()) name = getString(snapshot, "fullName", "");
                        if (name.isEmpty()) name = getString(snapshot, "username", "");
                        if (name.isEmpty()) name = "Customer";

                        String phone = getString(snapshot, "phone", "");
                        if (phone.isEmpty()) phone = getString(snapshot, "contact", "");
                        if (phone.isEmpty()) phone = getString(snapshot, "noTelp", "");
                        if (phone.isEmpty()) phone = getString(snapshot, "numberPhone", "");
                        if (phone.isEmpty()) phone = "Belum ada nomor";

                        tvCustomerName.setText(name);
                        tvCustomerContact.setText(phone);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(OrderDetailAdminActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderItems() {
        if (uid.isEmpty() || orderId.isEmpty()) return;

        ordersRef.child(uid).child(orderId).child("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        itemList.clear();

                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            OrderItemFirebase firebaseItem =
                                    itemSnapshot.getValue(OrderItemFirebase.class);

                            if (firebaseItem != null) {
                                itemList.add(new OrderItem(
                                        firebaseItem.getName(),
                                        firebaseItem.getPrice(),
                                        firebaseItem.getQuantity(),
                                        firebaseItem.getImageBase64(),
                                        firebaseItem.getCustomizeOptions(),
                                        firebaseItem.getNotes()
                                ));
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(OrderDetailAdminActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void selectStatus(String status, boolean showToast) {
        selectedStatus = status;
        tvOrderStatus.setText(status);

        resetStatusCard(btnStatusPending);
        resetStatusCard(btnStatusProcess);
        resetStatusCard(btnStatusReady);
        resetStatusCard(btnStatusSuccess);

        switch (status) {
            case "Pending":
                setActiveStatusCard(btnStatusPending);
                break;
            case "Process":
                setActiveStatusCard(btnStatusProcess);
                break;
            case "Ready":
                setActiveStatusCard(btnStatusReady);
                break;
            case "Done":
            case "Success":
                setActiveStatusCard(btnStatusSuccess);
                break;
        }

        if (showToast) {
            Toast.makeText(this, "Status " + status + " dipilih", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetStatusCard(MaterialCardView card) {
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#DDF5E8"));
        setChildColors(card, false);
    }

    private void setActiveStatusCard(MaterialCardView card) {
        card.setCardBackgroundColor(Color.parseColor("#006C46"));
        card.setStrokeWidth(0);
        setChildColors(card, true);
    }

    private void setChildColors(View view, boolean active) {
        int titleColor = active ? Color.WHITE : Color.parseColor("#002112");
        int accentColor = active ? Color.parseColor("#87FFD0") : Color.parseColor("#006C46");

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            CharSequence text = textView.getText();

            if (text != null && (
                    text.toString().equals("Menunggu") ||
                            text.toString().equals("Dimasak") ||
                            text.toString().equals("Siap diambil") ||
                            text.toString().equals("Selesai"))) {
                textView.setTextColor(accentColor);
            } else {
                textView.setTextColor(titleColor);
            }
            return;
        }

        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(active ? Color.WHITE : Color.parseColor("#006C46"));
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setChildColors(group.getChildAt(i), active);
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void saveStatus() {
        if (orderId.isEmpty() || uid.isEmpty()) {
            Toast.makeText(this,
                    "Order ID atau UID tidak ditemukan",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ordersRef.child(uid)
                .child(orderId)
                .child("status")
                .setValue(selectedStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Status berhasil diupdate ke " + selectedStatus,
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Gagal update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String getString(DataSnapshot snapshot, String key, String fallback) {
        Object value = snapshot.child(key).getValue();
        return value != null ? String.valueOf(value) : fallback;
    }
}