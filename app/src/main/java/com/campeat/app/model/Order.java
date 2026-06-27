package com.campeat.app.model;

public class Order {

    private String orderId;
    private String uid;

    private String customerName;
    private String customerContact;

    private String deliveryMethod;

    private String date;
    private String payment;
    private int point;
    private String status;
    private double total;

    public Order() {}

    // =========================
    // GETTER
    // =========================

    public String getOrderId() {
        return orderId;
    }

    public String getUid() {
        return uid;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getDate() {
        return date;
    }

    public String getPayment() {
        return payment;
    }

    public int getPoint() {
        return point;
    }

    public String getStatus() {
        return status;
    }

    public double getTotal() {
        return total;
    }

    // =========================
    // SETTER
    // =========================

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}