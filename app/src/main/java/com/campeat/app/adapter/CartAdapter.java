package com.campeat.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.CartItem;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItemList;
    private final Context context;
    private OnQuantityChangeListener quantityChangeListener;

    // ================= CONSTRUCTOR =================
    public CartAdapter(List<CartItem> cartItemList, Context context) {
        this.cartItemList = cartItemList;
        this.context = context;
    }

    // ================= LISTENER =================
    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        // ===== GAMBAR =====
        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            byte[] decoded = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            holder.imageItem.setImageBitmap(bitmap);
        } else {
            holder.imageItem.setImageResource(R.drawable.img_placeholder);
        }

        // ===== NAMA =====
        holder.textItemName.setText(item.getName());

        // ===== CUSTOMIZE OPTIONS =====
        if (item.getCustomizeOptions() != null
                && !item.getCustomizeOptions().isEmpty()) {
            holder.textCustomize.setVisibility(View.VISIBLE);
            holder.textCustomize.setText(item.getCustomizeOptions());
        } else {
            holder.textCustomize.setVisibility(View.GONE);
        }

        // ===== NOTES =====
        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            holder.textNotes.setVisibility(View.VISIBLE);
            holder.textNotes.setText("📝 " + item.getNotes());
        } else {
            holder.textNotes.setVisibility(View.GONE);
        }

        // ===== QUANTITY =====
        holder.textQuantity.setText("x" + item.getQuantity());

        // ===== HARGA =====
        double totalItemPrice = item.getPrice() * item.getQuantity();
        holder.textItemPrice.setText(formatRupiah(totalItemPrice));

        // ===== REMOVE =====
        holder.textRemove.setOnClickListener(v -> {
            removeItem(position);
            notifyPriceChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    private void removeItem(int position) {
        CartItem item = cartItemList.get(position);
        cartItemList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cartItemList.size());
        Toast.makeText(context,
                item.getName() + " dihapus dari keranjang",
                Toast.LENGTH_SHORT).show();
    }

    private void notifyPriceChanged() {
        if (quantityChangeListener != null) {
            quantityChangeListener.onQuantityChange();
        }
    }

    private String formatRupiah(double number) {
        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        return format.format(number);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChange();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imageItem;
        TextView textItemName;
        TextView textItemPrice;
        TextView textCustomize;
        TextView textNotes;
        TextView textQuantity;
        TextView textRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.image_item);
            textItemName = itemView.findViewById(R.id.text_item_name);
            textItemPrice = itemView.findViewById(R.id.text_item_price);
            textCustomize = itemView.findViewById(R.id.text_customize);
            textNotes = itemView.findViewById(R.id.text_notes);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            textRemove = itemView.findViewById(R.id.text_remove);
        }
    }
}