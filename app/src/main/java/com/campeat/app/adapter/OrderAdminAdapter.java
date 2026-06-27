package com.campeat.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.OrderDetailAdminActivity;
import com.campeat.app.R;
import com.campeat.app.model.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdminAdapter extends RecyclerView.Adapter<OrderAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdminAdapter(Context context,
                             List<Order> orderList,
                             OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_admin, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId());
        holder.tvOrderDate.setText(order.getDate());
        holder.tvOrderStatus.setText(order.getStatus());

        String customerName = order.getCustomerName();
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Customer";
        }
        holder.tvCustomerName.setText(customerName);

        Locale localeID = new Locale("id", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);

        holder.tvTotalPrice.setText(format.format(order.getTotal()));

        setStatusStyle(holder, order.getStatus());

        holder.tvOrderStatus.setClickable(false);
        holder.tvOrderStatus.setFocusable(false);

        holder.cardOrder.setOnClickListener(v -> openOrderDetail(order));

        holder.btnProcessOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(context, OrderDetailAdminActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        context.startActivity(intent);
    }

    private void setStatusStyle(ViewHolder holder, String status) {
        if (status == null) return;

        switch (status) {
            case "Pending":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                holder.tvOrderStatus.setTextColor(Color.WHITE);
                break;

            case "Process":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_process);
                holder.tvOrderStatus.setTextColor(Color.WHITE);
                break;

            case "Ready":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_done);
                holder.tvOrderStatus.setTextColor(Color.WHITE);
                break;

            case "Success":
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_badge_success);
                holder.tvOrderStatus.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardOrder;
        TextView tvOrderId;
        TextView tvCustomerName;
        TextView tvOrderDate;
        TextView tvOrderStatus;
        TextView tvTotalPrice;
        View btnProcessOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardOrder = itemView.findViewById(R.id.card_order);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvOrderDate = itemView.findViewById(R.id.tv_time);
            tvOrderStatus = itemView.findViewById(R.id.tv_status);
            tvTotalPrice = itemView.findViewById(R.id.tv_total);
            btnProcessOrder = itemView.findViewById(R.id.btn_process_order);
        }
    }
}