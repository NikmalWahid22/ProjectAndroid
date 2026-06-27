package com.campeat.app.model;

public class ReviewModel {

    private String uid;
    private String username;
    private float rating;
    private String review;
    private String date;

    // Required empty constructor Firebase
    public ReviewModel() {
    }

    public ReviewModel(
            String uid,
            String username,
            float rating,
            String review,
            String date
    ) {
        this.uid = uid;
        this.username = username;
        this.rating = rating;
        this.review = review;
        this.date = date;
    }

    // =========================
    // GETTER
    // =========================

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public float getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public String getDate() {
        return date;
    }



    // =========================
    // SETTER
    // =========================

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setDate(String date) {
        this.date = date;
    }
}