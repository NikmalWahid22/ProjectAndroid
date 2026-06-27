package com.campeat.app.model;

public class OrderItem {

    private String name;
    private int price;
    private int qty;

    private String imageBase64;
    private String customizeOptions;
    private String notes;

    public OrderItem() {
    }

    public OrderItem(
            String name,
            int price,
            int qty,
            String imageBase64,
            String customizeOptions,
            String notes
    ) {
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.imageBase64 = imageBase64;
        this.customizeOptions = customizeOptions;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public String getCustomizeOptions() {
        return customizeOptions;
    }

    public String getNotes() {
        return notes;
    }
}