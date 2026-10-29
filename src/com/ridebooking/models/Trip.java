package com.ridebooking.models;

public class Trip {
    private int id;
    private Rider rider;
    private Driver driver;
    private double distance;
    private double fare;
    private String status;         // REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED
    private String paymentStatus;  // PENDING, PAID
    private int rating;            // 0 means not yet rated, 1-5 once rated

    public Trip(int id, Rider rider, double distance) {
        this.id = id;
        this.rider = rider;
        this.distance = distance;
        this.fare = calculateFare(distance);
        this.driver = null;
        this.status = "REQUESTED";
        this.paymentStatus = "PENDING"1;
        this.rating = 0;
    }

    public static double calculateFare(double distance) {
        // Base fare ₹50 + ₹12 per kilometre
        return 50.0 + (distance * 12.0);
    }

    public int getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public double getDistance() {
        return distance;
    }

    public double getFare() {
        return fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
