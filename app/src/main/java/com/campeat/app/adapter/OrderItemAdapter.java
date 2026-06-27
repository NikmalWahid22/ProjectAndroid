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
import com.campeat.app.model.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final Context context;
    private final List<OrderItem> itemList;

    public OrderItemAdapter(Context context, List<OrderItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_order_item,
                        parent,
                        false
                );

        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull OrderItemViewHolder holder,
            int position
    ) {

        OrderItem item = itemList.get(position);

        holder.tvItemName.setText(item.getName());

        holder.tvItemPrice.setText(
                "Rp " + formatPrice(item.getPrice())
        );

        holder.tvQty.setText(item.getQty() + "x");

        // ================================
        // IMAGE BASE64
        // ================================
        if (item.getImageBase64() != null &&
                !item.getImageBase64().isEmpty()) {

            try {

                byte[] decodedBytes =
                        Base64.decode(
                                item.getImageBase64(),
                                Base64.DEFAULT
                        );

                Bitmap bitmap =
                        BitmapFactory.decodeByteArray(
                                decodedBytes,
                                0,
                                decodedBytes.length
                        );

                holder.imgItem.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ================================
        // CUSTOMIZE OPTIONS
        // ================================
        String customize =
                item.getCustomizeOptions();

        if (customize != null &&
                !customize.trim().isEmpty()) {

            holder.tvCustomizeOptions
                    .setVisibility(View.VISIBLE);

            holder.tvCustomizeOptions
                    .setText(customize);

        } else {

            holder.tvCustomizeOptions
                    .setVisibility(View.GONE);
        }

        // ================================
        // NOTES
        // ================================
        String notes = item.getNotes();

        if (notes != null &&
                !notes.trim().isEmpty()) {

            holder.tvItemNotes
                    .setVisibility(View.VISIBLE);

            holder.tvItemNotes.setText(
                    "Catatan: " + notes
            );

        } else {

            holder.tvItemNotes
                    .setVisibility(View.GONE);
        }

        // ================================
        // DIVIDER
        // ================================
        if (position == itemList.size() - 1) {

            holder.divider.setVisibility(View.GONE);

        } else {

            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    private String formatPrice(int price) {

        return String.format("%,d", price)
                .replace(",", ".");
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class OrderItemViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgItem;

        TextView tvItemName;
        TextView tvItemPrice;
        TextView tvQty;

        TextView tvCustomizeOptions;
        TextView tvItemNotes;

        View divider;

        public OrderItemViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgItem =
                    itemView.findViewById(R.id.img_item);

            tvItemName =
                    itemView.findViewById(R.id.tv_item_name);

            tvItemPrice =
                    itemView.findViewById(R.id.tv_item_price);

            tvQty =
                    itemView.findViewById(R.id.tv_qty);

            tvCustomizeOptions =
                    itemView.findViewById(
                            R.id.tv_customize_options
                    );

            tvItemNotes =
                    itemView.findViewById(
                            R.id.tv_item_notes
                    );

            divider =
                    itemView.findViewById(R.id.divider);
        }
    }
}