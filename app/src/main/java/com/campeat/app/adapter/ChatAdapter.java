package com.campeat.app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.ChatMessage;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    // ================================
    // VIEW HOLDERS
    // ================================
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        BotViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        UserViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    static class OrderCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvFoodName, tvEstimated, tvTime;
        ShapeableImageView imgFood;
        OrderCardViewHolder(View v) {
            super(v);
            tvOrderId   = v.findViewById(R.id.tv_order_id);
            tvStatus    = v.findViewById(R.id.tv_status);
            tvFoodName  = v.findViewById(R.id.tv_food_name);
            tvEstimated = v.findViewById(R.id.tv_estimated);
            tvTime      = v.findViewById(R.id.tv_time);
            imgFood     = v.findViewById(R.id.img_food);
        }
    }

    // ================================
    // GET ITEM VIEW TYPE
    // ================================
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    // ================================
    // ON CREATE VIEW HOLDER
    // ================================
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ChatMessage.TYPE_USER:
                return new UserViewHolder(
                        inflater.inflate(R.layout.item_chat_user, parent, false));
            case ChatMessage.TYPE_ORDER_CARD:
                return new OrderCardViewHolder(
                        inflater.inflate(R.layout.item_chat_order_card, parent, false));
            default: // TYPE_BOT
                return new BotViewHolder(
                        inflater.inflate(R.layout.item_chatbot, parent, false));
        }
    }

    // ================================
    // ON BIND VIEW HOLDER
    // ================================
    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage msg = messages.get(position);

        switch (msg.getType()) {

            case ChatMessage.TYPE_BOT:
                BotViewHolder bot = (BotViewHolder) holder;
                bot.tvMessage.setText(msg.getMessage());
                bot.tvTime.setText(msg.getTime());
                break;

            case ChatMessage.TYPE_USER:
                UserViewHolder user = (UserViewHolder) holder;
                user.tvMessage.setText(msg.getMessage());
                user.tvTime.setText(msg.getTime());
                break;

            case ChatMessage.TYPE_ORDER_CARD:
                OrderCardViewHolder card = (OrderCardViewHolder) holder;
                card.tvOrderId.setText(msg.getOrderId());
                card.tvFoodName.setText(msg.getFoodName());
                card.tvEstimated.setText("Estimated: " + msg.getEstimatedTime());
                card.tvTime.setText(msg.getTime());

                // Status + warna
                card.tvStatus.setText(msg.getStatus());
                applyStatusColor(card.tvStatus, msg.getStatus());

                // Gambar Base64
                if (msg.getImageBase64() != null && !msg.getImageBase64().isEmpty()) {
                    try {
                        byte[] decoded = Base64.decode(msg.getImageBase64(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        card.imgFood.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        card.imgFood.setImageResource(R.drawable.img_placeholder);
                    }
                } else {
                    card.imgFood.setImageResource(R.drawable.img_placeholder);
                }
                break;
        }
    }

    // ================================
    // STATUS COLOR
    // ================================
    private void applyStatusColor(TextView tv, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "pending":   tv.setTextColor(0xFFF59E0B); break;
            case "process":   tv.setTextColor(0xFF3B82F6); break;
            case "done":      tv.setTextColor(0xFF006C46); break;
            case "cancelled": tv.setTextColor(0xFFE53935); break;
            default:          tv.setTextColor(0xFF006C46); break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}