package com.campeat.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.OrderTrackingActivity;
import com.campeat.app.R;
import com.campeat.app.ReviewActivity;
import com.campeat.app.model.OrderModel;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    // ================================
    // INTERFACE CLICK LISTENER
    // ================================
    public interface OnOrderClickListener {
        void onOrderClick(OrderModel order);
    }

    private final ArrayList<OrderModel> list;
    private final OnOrderClickListener listener;

    public OrderAdapter(ArrayList<OrderModel> list,
                        OnOrderClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    // ================================
    // VIEW HOLDER
    // ================================
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderId, tvDate, tvPayment,
                tvStatus, tvTotal, tvPoint;

        AppCompatButton btnDetail;
        AppCompatButton btnRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId  = itemView.findViewById(R.id.tv_order_id);
            tvDate     = itemView.findViewById(R.id.tv_date);
            tvPayment  = itemView.findViewById(R.id.tv_payment);
            tvStatus   = itemView.findViewById(R.id.tv_status);
            tvTotal    = itemView.findViewById(R.id.tv_total);
            tvPoint    = itemView.findViewById(R.id.tv_point);
            btnDetail  = itemView.findViewById(R.id.btn_detail);
            btnRating  = itemView.findViewById(R.id.btn_rating);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OrderModel model = list.get(position);
        Context context  = holder.itemView.getContext();

        holder.tvOrderId.setText(formatOrderId(model.getOrderId()));
        holder.tvDate.setText(model.getDate() != null ? model.getDate() : "-");
        holder.tvPayment.setText(model.getPayment() != null ? model.getPayment() : "-");
        holder.tvStatus.setText(formatStatus(model.getStatus()));
        holder.tvTotal.setText(formatRupiah(model.getTotal()));
        holder.tvPoint.setText("+" + model.getPoint() + " point");

        applyStatusColor(holder.tvStatus, model.getStatus());

        // ================================
        // KLIK ITEM → OrderTrackingActivity
        // ================================
        View.OnClickListener toTracking = v -> {
            Intent intent = new Intent(context, OrderTrackingActivity.class);
            intent.putExtra("orderId", model.getOrderId());
            intent.putExtra("total",   model.getTotal());
            intent.putExtra("point",   model.getPoint());
            context.startActivity(intent);
        };

        holder.itemView.setOnClickListener(toTracking);
        holder.btnDetail.setOnClickListener(toTracking);

        // ================================
        // TOMBOL RATING — hanya saat Done
        // ================================
        if ("done".equalsIgnoreCase(model.getStatus())) {
            holder.btnRating.setVisibility(View.VISIBLE);
        } else {
            holder.btnRating.setVisibility(View.GONE);
        }

        holder.btnRating.setOnClickListener(v -> {

            String menuKey  = null;
            String menuName = "Menu";

            if (model.getItems() != null && !model.getItems().isEmpty()) {
                Map.Entry<String, Object> firstItem =
                        model.getItems().entrySet().iterator().next();

                menuKey = firstItem.getKey();

                Object value = firstItem.getValue();
                if (value instanceof Map) {
                    Object nameObj = ((Map) value).get("name");
                    if (nameObj != null) menuName = nameObj.toString();
                }
            }

            if (menuKey == null || menuKey.isEmpty()) {
                android.widget.Toast.makeText(context,
                        "Menu tidak valid", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, ReviewActivity.class);
            intent.putExtra("menuKey",  menuKey);
            intent.putExtra("menuName", menuName);
            context.startActivity(intent);
        });
    }

    // ================================
    // FORMAT ORDER ID
    // ================================
    private String formatOrderId(String orderId) {
        if (orderId == null || orderId.isEmpty()) return "#CE-000000";
        String shortId = orderId.length() > 6
                ? orderId.substring(orderId.length() - 6).toUpperCase()
                : orderId.toUpperCase();
        return "#CE-" + shortId;
    }

    // ================================
    // FORMAT STATUS
    // ================================
    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "Pending";
        switch (status.toLowerCase()) {
            case "pending":   return "Pending";
            case "process":   return "Process";
            case "ready":     return "Ready";
            case "done":      return "Done";
            case "cancelled": return "Cancelled";
            default:          return status;
        }
    }

    // ================================
    // STATUS COLOR
    // ================================
    private void applyStatusColor(TextView tvStatus, String status) {
        String value = status != null ? status.toLowerCase() : "";
        switch (value) {
            case "pending":   tvStatus.setTextColor(0xFFF59E0B); break;
            case "process":   tvStatus.setTextColor(0xFF3B82F6); break;
            case "done":      tvStatus.setTextColor(0xFF006C46); break;
            case "cancelled": tvStatus.setTextColor(0xFFE53935); break;
            default:          tvStatus.setTextColor(0xFF006C46); break;
        }
    }

    // ================================
    // FORMAT RUPIAH
    // ================================
    private String formatRupiah(double total) {
        return "Rp " + String.format("%,.0f", total).replace(",", ".");
    }

    @Override
    public int getItemCount() { return list.size(); }
}