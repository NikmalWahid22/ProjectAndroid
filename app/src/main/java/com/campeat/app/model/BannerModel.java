package com.campeat.app.model;

public class BannerModel {

    private String title;
    private String description;
    private String tag;
    private String imageBase64;
    private boolean active;

    private String key;

    // REQUIRED FIREBASE
    public BannerModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // CONSTRUCTOR
    public BannerModel(
            String title,
            String description,
            String tag,
            String imageBase64,
            boolean active
    ) {
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.imageBase64 = imageBase64;
        this.active = active;
    }

    // GETTER
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTag() {
        return tag;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public boolean isActive() {
        return active;
    }

    // SETTER
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}