package com.campeat.app.utils;

import com.campeat.app.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final List<CartItem> cartList = new ArrayList<>();

    // Tambah item ke cart
    public static void addItem(CartItem item) {
        for (CartItem c : cartList) {
            if (c.getId() == item.getId()) {
                c.setQuantity(c.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartList.add(item);
    }

    // Ambil semua item cart
    public static List<CartItem> getCart() {
        return cartList;
    }

    // Cek kosong
    public static boolean isEmpty() {
        return cartList.isEmpty();
    }

    // Hapus semua (misal setelah checkout)
    public static void clearCart() {
        cartList.clear();
    }
}

