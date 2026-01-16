package com.campeat.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.FoodAdapter;
import com.campeat.app.model.FoodItem;
import com.campeat.app.utils.SpaceItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private List<FoodItem> foodList;

    private static final int REQ_NOTIFICATION = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        createNotificationChannel();
        requestNotificationPermission();
        showWelcomeNotificationOnce();

        recyclerView = findViewById(R.id.recycler_food);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_bar);

        foodList = new ArrayList<>();

        setupFoodData();
        setupRecycler();
        setupBottomNav(bottomNav);
    }

    // ===================== RECYCLER =====================

    private void setupRecycler() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setPadding(8, 0, 8, 0);
        recyclerView.setClipToPadding(false);
        recyclerView.addItemDecoration(new SpaceItemDecoration(16));

        adapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(adapter);
    }

    private void setupFoodData() {
        foodList.add(new FoodItem(1, "Hamburger",
                "Hamburger lezat dengan roti lembut, daging juicy, sayuran segar, dan saus khas yang membuat setiap gigitan semakin nikmat. Cocok untuk makan siang cepat atau santai.",
                25000, R.drawable.example_food));

        foodList.add(new FoodItem(2, "Pizza",
                "Pizza dengan topping melimpah, keju yang lumer, dan aroma panggangan yang menggugah selera. Pilihan sempurna untuk dinikmati sendiri maupun bersama teman.",
                45000, R.drawable.pizza));

        foodList.add(new FoodItem(3, "Milk Tea",
                "Minuman milk tea creamy dengan perpaduan teh premium dan susu manis yang seimbang. Bisa dinikmati dengan topping boba atau jelly untuk sensasi lebih segar.",
                18000, R.drawable.milk_tea));

        foodList.add(new FoodItem(4, "Spaghetti",
                "Spaghetti autentik dengan saus pilihan seperti bolognese atau carbonara. Tekstur pasta yang pas dipadukan dengan bumbu kaya rasa.",
                35000, R.drawable.spagheti));

        foodList.add(new FoodItem(5, "Coffee",
                "Secangkir kopi aromatik yang dibuat dari biji kopi pilihan. Rasanya yang kuat namun seimbang memberikan energi dan kenyamanan setiap saat.",
                22000, R.drawable.coffee));
    }

    // ===================== BOTTOM NAV =====================

    private void setupBottomNav(BottomNavigationView bottomNav) {
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    // ===================== NOTIFICATION =====================

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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFICATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showWelcomeNotificationOnce();
        }
    }
}
