package com.campeat.app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.BannerModel;
import com.campeat.app.EditBannerAdminActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BannerAdminAdapter
        extends RecyclerView.Adapter<BannerAdminAdapter.ViewHolder> {

    private final Context context;
    private final List<BannerModel> list;

    public BannerAdminAdapter(Context context, List<BannerModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_banner_admin, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        BannerModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvDesc.setText(model.getDescription());
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

        // DELETE
        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Banner")
                    .setMessage("Yakin ingin menghapus banner?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        FirebaseDatabase.getInstance(
                                        ""
                                )
                                .getReference("banners")
                                .child(model.getKey())
                                .removeValue();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // EDIT
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditBannerAdminActivity.class);
            intent.putExtra("bannerKey", model.getKey());
            intent.putExtra("bannerTitle", model.getTitle());
            intent.putExtra("bannerDescription", model.getDescription());
            intent.putExtra("bannerTag", model.getTag());
            intent.putExtra("bannerImage", model.getImageBase64());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgBanner, btnEdit, btnDelete;

        TextView tvTitle, tvDesc, tvTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBanner = itemView.findViewById(R.id.img_banner);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvTag = itemView.findViewById(R.id.tv_tag);
        }
    }
}