package com.campeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campeat.app.R;
import com.campeat.app.model.MenuModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuAdminAdapter
        extends RecyclerView.Adapter<MenuAdminAdapter.ViewHolder> {

    private Context context;

    private List<MenuModel> menuList;

    private OnEditClickListener editListener;

    private OnDeleteClickListener deleteListener;

    // INTERFACE EDIT
    public interface OnEditClickListener {

        void onEditClick(MenuModel menu);

    }

    // INTERFACE DELETE
    public interface OnDeleteClickListener {

        void onDeleteClick(MenuModel menu);

    }

    // CONSTRUCTOR
    public MenuAdminAdapter(
            Context context,
            List<MenuModel> menuList,
            OnEditClickListener editListener,
            OnDeleteClickListener deleteListener
    ) {

        this.context = context;

        this.menuList = menuList;

        this.editListener = editListener;

        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(context)
                .inflate(
                        R.layout.item_menu_admin,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        MenuModel menu = menuList.get(position);

        android.util.Log.d(
                "MENU_DEBUG",
                "BIND = " + menu.getName()
        );

        // MENU NAME
        holder.tvMenuName.setText(
                menu.getName()
        );

        // CATEGORY
        holder.tvCategory.setText(
                menu.getCategory()
        );

        // PRICE
        Locale localeID =
                new Locale("id", "ID");

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        localeID
                );

        holder.tvPrice.setText(
                format.format(
                        menu.getPrice()
                )
        );

        // IMAGE - load dari Base64
        if (menu.getImageBase64() != null && !menu.getImageBase64().isEmpty()) {

            byte[] decodedBytes = android.util.Base64.decode(
                    menu.getImageBase64(), android.util.Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    decodedBytes, 0, decodedBytes.length);

            holder.imgMenu.setImageBitmap(bitmap);

        } else {
            // fallback ke placeholder kalau belum ada gambar
            holder.imgMenu.setImageResource(R.drawable.img_placeholder);
        }

        // LOW STOCK
        if (menu.getStock() <= 5) {

            holder.tvStock.setVisibility(
                    View.VISIBLE
            );

            holder.tvStock.setText(
                    "Low Stock : "
                            + menu.getStock()
            );

        } else {

            holder.tvStock.setVisibility(
                    View.GONE
            );
        }

        // EDIT
        holder.btnEdit.setOnClickListener(v -> {

            if (editListener != null) {

                editListener.onEditClick(menu);

            }

        });

        // DELETE
        holder.btnDelete.setOnClickListener(v -> {

            if (deleteListener != null) {

                deleteListener.onDeleteClick(menu);

            }

        });
    }

    @Override
    public int getItemCount() {

        return menuList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgMenu;

        ImageView btnEdit;

        ImageView btnDelete;

        TextView tvMenuName;

        TextView tvCategory;

        TextView tvPrice;

        TextView tvStock;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgMenu =
                    itemView.findViewById(
                            R.id.img_menu
                    );

            btnEdit =
                    itemView.findViewById(
                            R.id.btn_edit
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btn_delete
                    );

            tvMenuName =
                    itemView.findViewById(
                            R.id.tv_menu_name
                    );

            tvCategory =
                    itemView.findViewById(
                            R.id.tv_menu_category
                    );

            tvPrice =
                    itemView.findViewById(
                            R.id.tv_menu_price
                    );

            tvStock =
                    itemView.findViewById(
                            R.id.tv_stock
                    );
        }
    }
}