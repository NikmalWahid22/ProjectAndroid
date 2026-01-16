package com.campeat.app.model;

public class CartItem {

    private int id;          // ID makanan
    private String name;     // Nama makanan
    private double price;    // Harga (PAKAI double)
    private int quantity;    // Jumlah
    private int image;       // RESOURCE IMAGE (R.drawable.xxx)

    // ================= CONSTRUCTOR =================
    public CartItem(int id, String name, double price, int quantity, int image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }

    // ================= GETTER =================
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getImage() {
        return image;
    }

    // ================= SETTER =================
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
