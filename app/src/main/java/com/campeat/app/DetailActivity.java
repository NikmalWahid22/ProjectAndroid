package com.campeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campeat.app.model.CartItem;
import com.campeat.app.utils.CartManager;

public class DetailActivity extends AppCompatActivity {

    private int foodId;
    private String foodName;
    private String foodDesc;
    private int foodPrice;
    private int foodImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();

        foodId = intent.getIntExtra("ID", -1);
        foodName = intent.getStringExtra("NAME");
        foodDesc = intent.getStringExtra("DESC");
        foodPrice = intent.getIntExtra("PRICE", 0);
        foodImage = intent.getIntExtra("IMAGE", 0);

        if (foodId == -1) {
            Toast.makeText(this, "Data makanan tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        Button btnAddToCart = findViewById(R.id.btn_add_to_cart);
        TextView textName = findViewById(R.id.text_meal_name);
        TextView textDesc = findViewById(R.id.text_meal_description);
        TextView textPrice = findViewById(R.id.text_meal_price);
        ImageView imageFood = findViewById(R.id.image_meal);

        textName.setText(foodName);
        textDesc.setText(foodDesc);
        textPrice.setText("Rp " + foodPrice);
        imageFood.setImageResource(foodImage);

        btnBack.setOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> {

            CartItem item = new CartItem(
                    foodId,
                    foodName,
                    foodPrice,
                    1,
                    foodImage
            );

            CartManager.addItem(item);

            Toast.makeText(this, foodName + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, CartActivity.class));
        });
    }
}
