package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.OrderAdapter;
import com.campeat.app.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private RecyclerView recyclerOrder;
    private LinearLayout layoutEmpty;
    private ImageView btnBack;
    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // DATA
    // ================================
    private ArrayList<OrderModel> list;
    private OrderAdapter adapter;
    private DatabaseReference dbRef;

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        loadOrder();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        recyclerOrder = findViewById(R.id.recycler_order);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnBack = findViewById(R.id.btn_back);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);

        btnBack.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(this::finish, 100);
        });
    }

    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        list = new ArrayList<>();
        adapter = new OrderAdapter(list, order -> {
            // ke detail order
            Intent intent = new Intent(this, ReceiptActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("total", order.getTotal());
            intent.putExtra("payment", order.getPayment());
            intent.putExtra("point", order.getPoint());
            startActivity(intent);
        });
        recyclerOrder.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrder.setNestedScrollingEnabled(false);
        recyclerOrder.setAdapter(adapter);
    }

    // ================================
    // LOAD ORDER
    // ================================
    private void loadOrder() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef = FirebaseDatabase.getInstance(DB_URL)
                .getReference("orders")
                .child(uid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    OrderModel model = data.getValue(OrderModel.class);
                    if (model != null) {
                        list.add(model);
                    }
                }

                // reverse biar yang terbaru muncul di atas
                java.util.Collections.reverse(list);

                adapter.notifyDataSetChanged();

                // cek empty state
                if (list.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerOrder.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    recyclerOrder.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(OrderHistoryActivity.this,
                        "Gagal load order", Toast.LENGTH_SHORT).show();
            }
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
}