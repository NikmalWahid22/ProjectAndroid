package com.campeat.app.utils;

import com.campeat.app.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    // singleton list, persist selama app hidup
    private static final List<CartItem> cartItems = new ArrayList<>();

    // ================================
    // TAMBAH ITEM
    // ================================
    public static void addItem(CartItem newItem) {
        // cek apakah menu yang sama sudah ada di cart
        for (CartItem item : cartItems) {
            if (item.getMenuKey().equals(newItem.getMenuKey())
                    && item.getCustomizeOptions().equals(newItem.getCustomizeOptions())) {
                // kalau sama, tambah quantity aja
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        // kalau belum ada, tambah baru
        cartItems.add(newItem);
    }

    // ================================
    // HAPUS ITEM
    // ================================
    public static void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    // ================================
    // GET SEMUA ITEM
    // ================================
    public static List<CartItem> getItems() {
        return cartItems;
    }

    // ================================
    // TOTAL HARGA
    // ================================
    public static double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // ================================
    // TOTAL ITEM COUNT (untuk badge)
    // ================================
    public static int getTotalCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    // ================================
    // CLEAR CART (setelah checkout)
    // ================================
    public static void clearCart() {
        cartItems.clear();
    }

    // ================================
    // CEK APAKAH CART KOSONG
    // ================================
    public static boolean isEmpty() {
        return cartItems.isEmpty();
    }
}