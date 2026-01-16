package com.campeat.app.model;

public class FoodItem {

    private int id;
    private String name;
    private String description;
    private int price; // pakai int biar gampang hitung
    private int image;

    public FoodItem(int id, String name, String description, int price, int image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getImage() {
        return image;
    }
}
