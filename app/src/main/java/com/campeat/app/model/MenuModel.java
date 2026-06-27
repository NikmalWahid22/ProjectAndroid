package com.campeat.app.model;

public class MenuModel {

    private String key;
    private int id;
    private String name;
    private String description;
    private String image;
    private int price;
    private String category;
    private int stock;
    private String imageBase64;
    private boolean archived;
    private double rating;      // ← TAMBAH
    private int ratingCount;    // ← TAMBAH

    // EMPTY CONSTRUCTOR
    public MenuModel() {}

    // CONSTRUCTOR
    public MenuModel(
            int id,
            String name,
            String description,
            String image,
            int price,
            String category,
            int stock,
            boolean archived) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.archived = archived;
    }

    // GETTER
    public String getKey() { return key; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public boolean isArchived() { return archived; }
    public String getImageBase64() { return imageBase64; }
    public double getRating() { return rating; }           // ← TAMBAH
    public int getRatingCount() { return ratingCount; }    // ← TAMBAH

    // SETTER
    public void setKey(String key) { this.key = key; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image) { this.image = image; }
    public void setPrice(int price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setStock(int stock) { this.stock = stock; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setRating(double rating) { this.rating = rating; }          // ← TAMBAH
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; } // ← TAMBAH
}