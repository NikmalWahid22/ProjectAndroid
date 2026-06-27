package com.campeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.ReviewModel;

import java.util.List;

public class ReviewAdapter
        extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<ReviewModel> reviewList;

    public ReviewAdapter(Context context, List<ReviewModel> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_review, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ReviewModel review = reviewList.get(position);

        holder.tvUserName.setText(review.getUsername());
        holder.tvDate.setText(review.getDate());
        holder.tvRating.setText(String.format("%.1f", review.getRating()));
        holder.tvReview.setText(review.getReview());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvDate, tvRating, tvReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvReview = itemView.findViewById(R.id.tv_review);
        }
    }
}