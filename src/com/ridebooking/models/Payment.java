package com.ridebooking.models;

public class Payment {
    private int paymentId;
    private int tripId;
    private double amount;
    private String paymentStatus; // PENDING, PAID

    public Payment(int paymentId, int tripId, double amount, String paymentStatus) {
        this.paymentId = paymentId;
        this.tripId = tripId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getId() {
        return paymentId;
    }

    public int getTripId() {
        return tripId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
