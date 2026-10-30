package com.ridebooking.repository;

import com.ridebooking.models.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverRepository {
    private List<Driver> drivers = new ArrayList<>();

    public void saveDriver(Driver driver) {
        drivers.add(driver);
    }

    public void save(Driver driver) {
        saveDriver(driver);
    }

    public Driver findDriverById(int id) {
        for (Driver driver : drivers) {
            if (driver.getDriverId() == id) {
                return driver;
            }
        }
        return null;
    }

    public Driver findById(int id) {
        return findDriverById(id);
    }

    public Driver findAvailableDriver() {
        for (Driver driver : drivers) {
            if (driver.isAvailable()) {
                return driver;
            }
        }
        return null;
    }

    public List<Driver> getAllDrivers() {
        return drivers;
    }

    public List<Driver> findAll() {
        return getAllDrivers();
    }
}
