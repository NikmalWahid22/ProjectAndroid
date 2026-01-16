package com.campeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.CartItem;

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

    // ================= ADAPTER =================
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

        // ===== DATA =====
        holder.imageItem.setImageResource(item.getImage());
        holder.textItemName.setText(item.getName());
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));

        double totalItemPrice = item.getPrice() * item.getQuantity();
        holder.textItemPrice.setText(formatRupiah(totalItemPrice));

        // ===== ACTION MINUS =====
        holder.btnMinus.setOnClickListener(v -> {
            int qty = item.getQuantity() - 1;
            if (qty > 0) {
                item.setQuantity(qty);
                notifyItemChanged(position);
            } else {
                removeItem(position);
            }
            notifyPriceChanged();
        });

        // ===== ACTION PLUS =====
        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            notifyPriceChanged();
        });

        // ===== ACTION REMOVE =====
        holder.textRemove.setOnClickListener(v -> {
            removeItem(position);
            notifyPriceChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    // ================= LOGIC =================
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

    // ================= UTIL =================
    private String formatRupiah(double number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String result = formatRupiah.format(number).replace("Rp", "Rp ").trim();
        return result.substring(0, 2) + " " + result.substring(3);
    }

    // ================= INTERFACE =================
    public interface OnQuantityChangeListener {
        void onQuantityChange();
    }

    // ================= VIEWHOLDER =================
    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView imageItem;
        TextView textItemName;
        TextView textItemPrice;
        TextView btnPlus;
        TextView btnMinus;
        TextView textQuantity;
        TextView textRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            imageItem = itemView.findViewById(R.id.image_item);
            textItemName = itemView.findViewById(R.id.text_item_name);
            textItemPrice = itemView.findViewById(R.id.text_item_price);

            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            textQuantity = itemView.findViewById(R.id.text_quantity);

            textRemove = itemView.findViewById(R.id.text_remove);
        }
    }
}
