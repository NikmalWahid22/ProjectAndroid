package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.MenuAdminAdapter;
import com.campeat.app.model.MenuModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ManageMenuAdminActivity
        extends AppCompatActivity {

    // VIEW
    private EditText etSearchMenu;

    private TextView tvTotalItemCount;
    private TextView tvCategoriesCount;
    private TextView tvLowStockCount;
    private TextView tvLiveStockCount;

    private LinearLayout navHome;
    private LinearLayout navOrders;
    private LinearLayout navProfile;

    private RecyclerView recyclerMenu;

    // FIREBASE
    private DatabaseReference menusRef;

    // LIST
    private List<MenuModel> menuList;
    private List<MenuModel> filteredList;

    // ADAPTER
    private MenuAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_manage_menu
        );

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();

        setupRecyclerView();

        setupBottomNav();

        setupSearch();

        loadMenusFromFirebase();

        loadCategoriesCount();

        findViewById(R.id.btn_tambah_menu)
                .setOnClickListener(v -> {

                    startActivity(
                            new Intent(
                                    ManageMenuAdminActivity.this,
                                    AddMenuAdminActivity.class
                            )
                    );

                });
    }

    private void initViews() {

        etSearchMenu =
                findViewById(R.id.et_search_menu);

        tvTotalItemCount =
                findViewById(R.id.tv_total_item_count);

        tvCategoriesCount =
                findViewById(R.id.tv_categories_count);

        tvLowStockCount =
                findViewById(R.id.tv_low_stock_count);

        tvLiveStockCount =
                findViewById(R.id.tv_live_stock_count);

        navHome =
                findViewById(R.id.nav_home);

        navOrders =
                findViewById(R.id.nav_orders);

        navProfile =
                findViewById(R.id.nav_profile);

        recyclerMenu =
                findViewById(R.id.rv_menu);

        menuList = new ArrayList<>();

        filteredList = new ArrayList<>();
    }

    private void loadCategoriesCount() {
        FirebaseDatabase.getInstance(
                        ""
                ).getReference("categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvCategoriesCount.setText(
                                String.valueOf(snapshot.getChildrenCount())
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
    private void setupRecyclerView() {

        adapter = new MenuAdminAdapter(
                this,
                filteredList,
                menu -> openEditMenu(menu),
                menu -> deleteMenu(menu)
        );

        recyclerMenu.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerMenu.setHasFixedSize(true);

        recyclerMenu.setAdapter(adapter);

        recyclerMenu.setNestedScrollingEnabled(false);
    }
    private void setupBottomNav() {

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(ManageMenuAdminActivity.this, HomeAdminActivity.class));
            finish();
        });

        navOrders.setOnClickListener(v -> {
            // sudah di halaman manage menu, ga perlu pindah
            Toast.makeText(this, "Anda sudah di halaman ini", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(ManageMenuAdminActivity.this, ProfileAdminActivity.class));
            finish();
        });
    }

    private void setupSearch() {

        etSearchMenu.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterMenu(
                                s.toString()
                        );

                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {

                    }
                });
    }

    private void filterMenu(String query) {

        filteredList.clear();

        if (query.isEmpty()) {

            filteredList.addAll(menuList);

        } else {

            for (MenuModel menu : menuList) {

                String name =
                        menu.getName();

                if (name != null &&
                        name.toLowerCase()
                                .contains(
                                        query.toLowerCase()
                                )) {

                    filteredList.add(menu);

                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadMenusFromFirebase() {

        menusRef = FirebaseDatabase
                .getInstance("")
                .getReference("menus");

        menusRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        menuList.clear();

                        for (DataSnapshot dataSnapshot
                                : snapshot.getChildren()) {

                            MenuModel menu =
                                    dataSnapshot.getValue(
                                            MenuModel.class
                                    );

                            if (menu != null) {

                                menu.setKey(
                                        dataSnapshot.getKey()
                                );

                                // fallback data lama
                                if (menu.getCategory() == null) {

                                    menu.setCategory("Makanan");

                                }

                                // tampilkan semua menu
                                menuList.add(menu);
                            }
                        }

                        filteredList.clear();

                        filteredList.addAll(menuList);

                        System.out.println("TOTAL MENU : " + menuList.size());

                        adapter.notifyDataSetChanged();

                        updateStats();
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                ManageMenuAdminActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateStats() {

        tvTotalItemCount.setText(
                String.valueOf(
                        menuList.size()
                )
        );

        int lowStock = 0;
        int liveStock = 0;

        for (MenuModel menu : menuList) {

            // LOW STOCK
            if (menu.getStock() <= 5) {
                lowStock++;
            }

            // LIVE STOCK
            if (menu.getStock() > 0) {
                liveStock++;
            }
        }

        tvLowStockCount.setText(String.valueOf(lowStock));
        tvLiveStockCount.setText(String.valueOf(liveStock));

        // categories dihitung dari Firebase node categories
        // via loadCategoriesCount() — bukan dari data menu
    }

    private void openEditMenu(
            MenuModel menu
    ) {

        Intent intent =
                new Intent(
                        this,
                        EditMenuAdminActivity.class
                );

        intent.putExtra(
                "menuKey",
                menu.getKey()
        );

        intent.putExtra(
                "menuName",
                menu.getName()
        );

        intent.putExtra(
                "menuPrice",
                menu.getPrice()
        );

        intent.putExtra(
                "menuStock",
                menu.getStock()
        );

        intent.putExtra(
                "menuCategory",
                menu.getCategory()
        );

        intent.putExtra(
                "menuDescription",
                menu.getDescription()
        );

        intent.putExtra(
                "menuImage",
                menu.getImage()
        );

        intent.putExtra(
                "menuImageBase64",
                menu.getImageBase64()
        );


        startActivity(intent);
    }

    private void deleteMenu(MenuModel menu) {

        if (menu.getKey() == null) {
            Toast.makeText(this, "Menu tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Menu")
                .setMessage("Yakin mau hapus \"" + menu.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {

                    menusRef.child(menu.getKey())
                            .removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(
                                    this,
                                    "Menu berhasil dihapus",
                                    Toast.LENGTH_SHORT
                            ).show())
                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    "Gagal menghapus menu",
                                    Toast.LENGTH_SHORT
                            ).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}