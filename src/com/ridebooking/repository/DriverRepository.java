package com.ridebooking.repository;

import com.ridebooking.models.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverRepository {
    private List<Driver> drivers = new ArrayList<>();

    public void save(Driver driver) {
        drivers.add(driver);
    }

    public Driver findById(int id) {
        for (Driver driver : drivers) {
            if (driver.getId() == id) {
                return driver;
            }
        }
        return null;
    }

    public List<Driver> findAll() {
        return drivers;
    }

    public Driver findAvailableDriver() {
        for (Driver driver : drivers) {
            if (driver.isAvailable()) {
                return driver;
            }
        }
        return null;
    }
}
