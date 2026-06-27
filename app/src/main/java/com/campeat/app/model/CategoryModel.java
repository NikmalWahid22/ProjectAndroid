package com.campeat.app.model;

public class CategoryModel {

    private String key;
    private String name;

    public CategoryModel() {}

    public CategoryModel(String name) {
        this.name = name;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}