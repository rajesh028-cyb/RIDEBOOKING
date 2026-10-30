package com.ridebooking.models;

public class Rider {
    private int riderId;
    private String name;
    private String phoneNumber;

    public Rider(int riderId, String name, String phoneNumber) {
        this.riderId = riderId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getRiderId() {
        return riderId;
    }

    public int getId() {
        return riderId;
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
}
