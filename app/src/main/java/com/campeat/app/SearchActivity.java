package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.adapter.SearchResultAdapter;
import com.campeat.app.model.FoodItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    EditText inputSearch;
    RecyclerView recyclerSearch;

    List<FoodItem> fullFoodList = new ArrayList<>();
    List<FoodItem> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        inputSearch = findViewById(R.id.input_search_food);
        recyclerSearch = findViewById(R.id.recycler_food_items);

        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));

        loadDummyData();
        SearchResultAdapter adapter = new SearchResultAdapter(filteredList, this);
        recyclerSearch.setAdapter(adapter);

        setupSearchListener(adapter);
        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_bar);

        // Set nav SEARCH as active
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                return true; // already here
            }

            if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupSearchListener(SearchResultAdapter adapter) {
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s.toString(), adapter);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterResults(String keyword, SearchResultAdapter adapter) {
        filteredList.clear();
        if (!keyword.isEmpty()) {
            for (FoodItem item : fullFoodList) {
                if (item.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadDummyData() {

        fullFoodList.add(new FoodItem(
                1,
                "Hamburger",
                "Hamburger lezat dengan roti lembut, daging juicy, sayuran segar, dan saus khas yang membuat setiap gigitan semakin nikmat. Cocok untuk makan siang cepat atau santai.",
                25000,
                R.drawable.example_food
        ));

        fullFoodList.add(new FoodItem(
                2,
                "Pizza",
                "Pizza dengan topping melimpah, keju yang lumer, dan aroma panggangan yang menggugah selera. Pilihan sempurna untuk dinikmati sendiri maupun bersama teman.",
                45000,
                R.drawable.pizza
        ));

        fullFoodList.add(new FoodItem(
                3,
                "Milk Tea",
                "Minuman milk tea creamy dengan perpaduan teh premium dan susu manis yang seimbang. Bisa dinikmati dengan topping boba atau jelly untuk sensasi lebih segar.",
                18000,
                R.drawable.milk_tea
        ));

        fullFoodList.add(new FoodItem(
                4,
                "Spaghetti",
                "Spaghetti autentik dengan saus pilihan seperti bolognese atau carbonara. Tekstur pasta yang pas dipadukan dengan bumbu kaya rasa.",
                35000,
                R.drawable.spagheti
        ));

        fullFoodList.add(new FoodItem(
                5,
                "Coffee",
                "Secangkir kopi aromatik yang dibuat dari biji kopi pilihan. Rasanya yang kuat namun seimbang memberikan energi dan kenyamanan setiap saat.",
                22000,
                R.drawable.coffee
        ));
    }
}
