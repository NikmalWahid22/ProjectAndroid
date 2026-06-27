package com.campeat.app.adapter;

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
import com.campeat.app.model.BannerModel;

import java.util.List;

public class BannerAdapter
        extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    private final List<BannerModel> list;

    public BannerAdapter(List<BannerModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        BannerModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvSubtitle.setText(model.getDescription());
        holder.tvTag.setText(model.getTag());

        try {

            byte[] bytes =
                    Base64.decode(model.getImageBase64(), Base64.DEFAULT);

            Bitmap bitmap =
                    BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.length
                    );

            holder.imgBanner.setImageBitmap(bitmap);

        } catch (Exception e) {

            holder.imgBanner.setImageResource(R.drawable.img_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgBanner;

        TextView tvTitle, tvSubtitle, tvTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBanner = itemView.findViewById(R.id.img_banner);

            tvTitle = itemView.findViewById(R.id.tv_banner_title);
            tvSubtitle = itemView.findViewById(R.id.tv_banner_subtitle);
            tvTag = itemView.findViewById(R.id.tv_banner_tag);
        }
    }
}