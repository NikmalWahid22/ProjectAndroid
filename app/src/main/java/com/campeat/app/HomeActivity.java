package com.campeat.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.campeat.app.adapter.FoodUserAdapter;
import com.campeat.app.model.MenuModel;
import com.campeat.app.adapter.BannerAdapter;
import com.campeat.app.model.BannerModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // ================================
    // VIEWS
    // ================================
    private RecyclerView rvMenu;
    private ViewPager2 viewPagerBanner;
    private ArrayList<BannerModel> bannerList;
    private LinearLayout layoutCategories;
    private LinearLayout navHome, navSearch, navCart, navProfile;
    private androidx.cardview.widget.CardView fabChatbot;


    // ================================
    // FIREBASE
    // ================================
    private DatabaseReference dbRef;

    // ================================
    // LIST
    // ================================
    private List<MenuModel> menuList;
    private List<MenuModel> filteredList;

    // ================================
    // ADAPTER
    // ================================
    private FoodUserAdapter adapter;
    private BannerAdapter bannerAdapter;

    // ================================
    // SELECTED CATEGORY
    // ================================
    private String selectedCategory = "All";

    private static final int REQ_NOTIFICATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbRef = FirebaseDatabase.getInstance(
                ""
        ).getReference();

        createNotificationChannel();
        requestNotificationPermission();
        showWelcomeNotificationOnce();

        getWindow().setStatusBarColor(Color.parseColor("#EEF8F2"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        setupRecyclerView();
        setupBottomNav();
        setupChatbotFab();
        loadBanner();
        loadCategories();
        loadMenus();
    }

    // ================================
    // INIT VIEWS
    // ================================
    @SuppressLint("WrongViewCast")
    private void initViews() {
        rvMenu = findViewById(R.id.rv_menu);
        layoutCategories = findViewById(R.id.layout_categories);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navCart = findViewById(R.id.nav_cart);
        navProfile = findViewById(R.id.nav_profile);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);
        fabChatbot = findViewById(R.id.fab_chatbot);
    }

    // ================================
    // SETUP RECYCLERVIEW
    // ================================
    private void setupRecyclerView() {
        menuList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new FoodUserAdapter(
                this,
                filteredList,
                menu -> openDetailMenu(menu)
        );

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.setNestedScrollingEnabled(false);
        rvMenu.setAdapter(adapter);
    }

    private void setupChatbotFab() {
        fabChatbot.setOnClickListener(v -> {
            animateNavClick(v);
            v.postDelayed(() -> {
                startActivity(new Intent(this, ChatbotActivity.class));
                overridePendingTransition(R.anim.modern_enter, R.anim.modern_exit);
            }, 100);
        });
    }

    // ================================
    // SETUP BOTTOM NAV
    // ================================
    private void setupBottomNav() {
        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Anda sudah di Home", Toast.LENGTH_SHORT).show()
        );

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

    // ================================
// LOAD BANNER
// ================================
    private void loadBanner() {

        bannerList = new ArrayList<>();

        dbRef.child("banners")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        bannerList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            BannerModel banner =
                                    data.getValue(BannerModel.class);

                            if (banner != null) {

                                banner.setKey(data.getKey());

                                if (banner.isActive()) {
                                    bannerList.add(banner);
                                }
                            }
                        }

                        bannerAdapter =
                                new BannerAdapter(bannerList);

                        viewPagerBanner.setAdapter(bannerAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    // ================================
    // LOAD CATEGORIES
    // ================================
    private void loadCategories() {
        dbRef.child("categories").addValueEventListener(new ValueEventListener() {
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
                String chipText = tv.getText().toString();
                boolean active = chipText.equals(name.equals("All") ? "All Item" : name);
                updateChipStyle(tv, active);
            }

            filterByCategory(name);
        });

        layoutCategories.addView(chip);
    }

    private void updateChipStyle(TextView tv, boolean isSelected) {
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_chip_selected);
            tv.setTextColor(getResources().getColor(android.R.color.white, null));
        } else {
            tv.setBackgroundResource(R.drawable.bg_chip_unselected);
            tv.setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    // ================================
    // LOAD MENUS
    // ================================
    private void loadMenus() {
        dbRef.child("menus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                menuList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    MenuModel menu = data.getValue(MenuModel.class);
                    if (menu != null && !menu.isArchived()) {
                        menu.setKey(data.getKey());
                        menuList.add(menu);
                    }
                }

                filterByCategory(selectedCategory);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HomeActivity.this,
                        "Gagal load menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================================
    // FILTER BY CATEGORY
    // ================================
    private void filterByCategory(String category) {
        filteredList.clear();

        if (category.equals("All")) {
            filteredList.addAll(menuList);
        } else {
            for (MenuModel menu : menuList) {
                if (menu.getCategory() != null &&
                        menu.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(menu);
                }
            }
        }

        adapter.notifyDataSetChanged();
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
    // NOTIFICATION
    // ================================
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "welcome_channel",
                    "Welcome Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Welcome notification");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION
                );
            }
        }
    }

    private void showWelcomeNotificationOnce() {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "welcome_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(getString(R.string.notif_welcome_title))
                        .setContentText(getString(R.string.notif_welcome_text))
                        .setAutoCancel(true);
        if (manager != null) manager.notify(1001, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showWelcomeNotificationOnce();
        }
    }
}