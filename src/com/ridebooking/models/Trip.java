package com.ridebooking.models;

public class Trip {
    private int tripId;
    private Rider rider;
    private Driver driver;
    private String pickupLocation;
    private String dropLocation;
    private double distance;
    private double fare;
    private String tripStatus; // REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED

    public Trip(int tripId, Rider rider, String pickupLocation, String dropLocation, double distance) {
        this.tripId = tripId;
        this.rider = rider;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.distance = distance;
        this.fare = calculateFare(distance);
        this.driver = null;
        this.tripStatus = "REQUESTED";
    }

    public Trip(int tripId, Rider rider, double distance) {
        this(tripId, rider, "Pickup", "Drop", distance);
    }

    public static double calculateFare(double distance) {
        // Base fare ₹50 + ₹12 per kilometre
        return 50.0 + (distance * 12.0);
    }

    public int getTripId() {
        return tripId;
    }

    public int getId() {
        return tripId;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
        this.fare = calculateFare(distance);
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public String getStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }

    public void setStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }
}
