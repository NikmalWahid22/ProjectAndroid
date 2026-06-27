package com.campeat.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.FoodUserAdapter;
import com.campeat.app.model.MenuModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private EditText inputSearch;
    private RecyclerView recyclerSearch;
    private LinearLayout layoutCategories;
    private LinearLayout layoutEmpty;
    private TextView tvResultLabel;
    private LinearLayout navHome, navSearch, navCart, navProfile;

    // ================================
    // DATA
    // ================================
    private List<MenuModel> fullList = new ArrayList<>();
    private List<MenuModel> filteredList = new ArrayList<>();
    private FoodUserAdapter adapter;
    private String selectedCategory = "All";

    private final String DB_URL =
            "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        loadCategories();
        loadMenus();
        setupSearchListener();
    }

    // ================================
    // INIT VIEWS
    // ================================
    private void initViews() {
        inputSearch = findViewById(R.id.input_search_food);
        recyclerSearch = findViewById(R.id.recycler_food_items);
        layoutCategories = findViewById(R.id.layout_categories);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvResultLabel = findViewById(R.id.tv_result_label);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
    }

    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        adapter = new FoodUserAdapter(
                this,
                filteredList,
                menu -> openDetailMenu(menu)
        );
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearch.setNestedScrollingEnabled(false);
        recyclerSearch.setAdapter(adapter);
    }

    // ================================
    // LOAD CATEGORIES
    // ================================
    private void loadCategories() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutCategories.removeAllViews();
                        addCategoryChip("All", true);
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String name = data.child("name").getValue(String.class);
                            if (name != null) addCategoryChip(name, false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ================================
    // ADD CATEGORY CHIP
    // ================================
    private void addCategoryChip(String name, boolean isSelected) {
        View chip = LayoutInflater.from(this)
                .inflate(R.layout.item_category_chip, layoutCategories, false);

        TextView tvChip = chip.findViewById(R.id.tv_chip);
        tvChip.setText(name.equals("All") ? "All Item" : name);
        updateChipStyle(tvChip, isSelected);

        chip.setOnClickListener(v -> {
            selectedCategory = name;
            for (int i = 0; i < layoutCategories.getChildCount(); i++) {
                View child = layoutCategories.getChildAt(i);
                TextView tv = child.findViewById(R.id.tv_chip);
                boolean active = tv.getText().toString()
                        .equals(name.equals("All") ? "All Item" : name);
                updateChipStyle(tv, active);
            }
            applyFilter(inputSearch.getText().toString());
        });

        layoutCategories.addView(chip);
    }

    private void updateChipStyle(TextView tv, boolean isSelected) {
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_chip_selected);
            tv.setTextColor(getResources().getColor(android.R.color.white, null));
        } else {
            tv.setBackgroundResource(R.drawable.bg_chip_unselected);
            tv.setTextColor(0xFF002112);
        }
    }

    // ================================
    // LOAD MENUS FROM FIREBASE
    // ================================
    private void loadMenus() {
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("menus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        fullList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            MenuModel menu = data.getValue(MenuModel.class);
                            if (menu != null && !menu.isArchived()) {
                                menu.setKey(data.getKey());
                                fullList.add(menu);
                            }
                        }
                        applyFilter(inputSearch.getText().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SearchActivity.this,
                                "Gagal load menu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================================
    // SEARCH LISTENER
    // ================================
    private void setupSearchListener() {
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ================================
    // APPLY FILTER (search + category)
    // ================================
    private void applyFilter(String keyword) {
        filteredList.clear();

        for (MenuModel menu : fullList) {
            boolean matchCategory = selectedCategory.equals("All")
                    || (menu.getCategory() != null
                    && menu.getCategory().equalsIgnoreCase(selectedCategory));

            boolean matchKeyword = keyword.isEmpty()
                    || (menu.getName() != null
                    && menu.getName().toLowerCase().contains(keyword.toLowerCase()));

            if (matchCategory && matchKeyword) {
                filteredList.add(menu);
            }
        }

        adapter.notifyDataSetChanged();

        // update label
        if (!keyword.isEmpty()) {
            tvResultLabel.setText("Hasil untuk \"" + keyword + "\"");
        } else if (!selectedCategory.equals("All")) {
            tvResultLabel.setText(selectedCategory);
        } else {
            tvResultLabel.setText("Semua Menu");
        }

        // empty state
        if (filteredList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerSearch.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerSearch.setVisibility(View.VISIBLE);
        }
    }

    // ================================
    // OPEN DETAIL MENU
    // ================================
    private void openDetailMenu(MenuModel menu) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("menuKey", menu.getKey());
        intent.putExtra("menuName", menu.getName());
        intent.putExtra("menuPrice", menu.getPrice());
        intent.putExtra("menuCategory", menu.getCategory());
        intent.putExtra("menuDescription", menu.getDescription());
        intent.putExtra("menuImageBase64", menu.getImageBase64());
        startActivity(intent);
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

        navSearch.setOnClickListener(v ->
                Toast.makeText(this, "Anda sudah di Search", Toast.LENGTH_SHORT).show()
        );

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