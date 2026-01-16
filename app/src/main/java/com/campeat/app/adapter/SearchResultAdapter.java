package com.campeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.FoodItem;
import com.campeat.app.DetailActivity;

import android.content.Intent;
import java.util.List;

public class  SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<FoodItem> list;
    private Context context;

    public SearchResultAdapter(List<FoodItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = list.get(position);

        holder.foodName.setText(item.getName());
        holder.foodDesc.setText(item.getDescription());
        holder.foodPrice.setText("Rp " + item.getPrice());
        holder.foodImg.setImageResource(item.getImage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("ID", item.getId());
            intent.putExtra("NAME", item.getName());
            intent.putExtra("DESC", item.getDescription());
            intent.putExtra("PRICE", item.getPrice());
            intent.putExtra("IMAGE", item.getImage());
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodDesc, foodPrice;
        ImageView foodImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            foodName = itemView.findViewById(R.id.item_food_name);
            foodDesc = itemView.findViewById(R.id.item_food_desc);
            foodPrice = itemView.findViewById(R.id.item_food_price);
            foodImg = itemView.findViewById(R.id.item_food_image);
        }
    }
}
