package com.campeat.app.model;

public class User {
    private String email;
    private String name;
    private int point;
    private String role;

    // Required empty constructor for Firebase
    public User() {}

    public User(String email, String name, int point, String role) {
        this.email = email;
        this.name = name;
        this.point = point;
        this.role = role;
    }

    // Getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public int getPoint() { return point; }
    public String getRole() { return role; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setPoint(int point) { this.point = point; }
    public void setRole(String role) { this.role = role; }
}