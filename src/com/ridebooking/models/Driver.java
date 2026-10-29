package com.ridebooking.models;

public class Driver {
    private int id;
    private String name;
    private String phone;
    private boolean available;

    public Driver(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.available = true; // New drivers are available by default
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
