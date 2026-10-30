package com.ridebooking.models;

public class Driver {
    private int driverId;
    private String name;
    private String phoneNumber;
    private boolean availability;

    public Driver(int driverId, String name, String phoneNumber) {
        this.driverId = driverId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.availability = true; // Initial availability is true
    }

    public int getDriverId() {
        return driverId;
    }

    public int getId() {
        return driverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAvailable() {
        return availability;
    }

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public void setAvailable(boolean availability) {
        this.availability = availability;
    }
}
