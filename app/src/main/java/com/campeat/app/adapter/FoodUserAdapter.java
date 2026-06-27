package com.campeat.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.MenuModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodUserAdapter
        extends RecyclerView.Adapter<FoodUserAdapter.ViewHolder> {

    private Context context;
    private List<MenuModel> menuList;
    private OnItemClickListener listener;

    // INTERFACE
    public interface OnItemClickListener {
        void onItemClick(MenuModel menu);
    }

    // CONSTRUCTOR
    public FoodUserAdapter(
            Context context,
            List<MenuModel> menuList,
            OnItemClickListener listener) {

        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menu = menuList.get(position);

        // NAMA
        holder.tvFoodName.setText(menu.getName());

        // CATEGORY
        holder.tvFoodCategory.setText(menu.getCategory());

        // HARGA
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        holder.tvFoodPrice.setText(format.format(menu.getPrice()));

        // RATING
        if (menu.getRating() > 0) {
            holder.tvRating.setVisibility(View.VISIBLE);
            holder.tvRating.setText(
                    String.format(Locale.getDefault(), "⭐ %.1f", menu.getRating())
            );
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }

        // GAMBAR
        if (menu.getImageBase64() != null && !menu.getImageBase64().isEmpty()) {
            byte[] decodedBytes = Base64.decode(
                    menu.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    decodedBytes, 0, decodedBytes.length);
            holder.imgFood.setImageBitmap(bitmap);
        } else {
            holder.imgFood.setImageResource(R.drawable.img_placeholder);
        }

        // TOMBOL PLUS → ke detail
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(menu);
        });

        // CARD CLICK → ke detail juga
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(menu);
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgFood;
        TextView tvFoodName;
        TextView tvFoodCategory;
        TextView tvFoodPrice;
        TextView tvRating;
        ImageView btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvFoodCategory = itemView.findViewById(R.id.tv_food_category);
            tvFoodPrice = itemView.findViewById(R.id.tv_food_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}