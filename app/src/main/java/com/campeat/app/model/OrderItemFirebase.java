package com.campeat.app.model;

public class OrderItemFirebase {

    private String name;
    private int price;
    private int quantity;
    private String notes;
    private String customizeOptions;

    // TAMBAHAN
    private String imageBase64;

    public OrderItemFirebase() {
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    public String getCustomizeOptions() {
        return customizeOptions;
    }

    // GET IMAGE
    public String getImageBase64() {
        return imageBase64;
    }
}