package com.campeat.app.model;

public class CartItem {

    private String menuKey;      // key Firebase
    private String name;
    private double price;
    private int quantity;
    private String imageBase64;  // ganti dari int image
    private String customizeOptions; // pilihan customize
    private String notes;        // catatan order

    // ================= CONSTRUCTOR BARU =================
    public CartItem(
            String menuKey,
            String name,
            double price,
            int quantity,
            String imageBase64,
            String customizeOptions,
            String notes) {

        this.menuKey = menuKey;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageBase64 = imageBase64;
        this.customizeOptions = customizeOptions;
        this.notes = notes;
    }

    // ================= GETTER =================
    public String getMenuKey() { return menuKey; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageBase64() { return imageBase64; }
    public String getCustomizeOptions() { return customizeOptions; }
    public String getNotes() { return notes; }

    // ================= SETTER =================
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setNotes(String notes) { this.notes = notes; }
}