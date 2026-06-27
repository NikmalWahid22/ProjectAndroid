package com.campeat.app.model;

import java.util.Map;

public class OrderModel {

    private String orderId;
    private String customerName;
    private String date;
    private String payment;
    private String status;
    private double total;
    private int point;

    // tambahan
    private String time;

    // FIX ITEMS
    private Map<String, Object> items;

    public OrderModel() {
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDate() {
        return date;
    }

    public String getPayment() {
        return payment;
    }

    public String getStatus() {
        return status;
    }

    public double getTotal() {
        return total;
    }

    public int getPoint() {
        return point;
    }

    public String getTime() {
        return time;
    }

    public Map<String, Object> getItems() {
        return items;
    }
}