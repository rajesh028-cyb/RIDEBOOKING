package com.ridebooking.repository;

import com.ridebooking.models.Rider;
import java.util.ArrayList;
import java.util.List;

public class RiderRepository {
    private List<Rider> riders = new ArrayList<>();

    public void saveRider(Rider rider) {
        riders.add(rider);
    }

    public void save(Rider rider) {
        saveRider(rider);
    }

    public Rider findRiderById(int id) {
        for (Rider rider : riders) {
            if (rider.getRiderId() == id) {
                return rider;
            }
        }
        return null;
    }

    public Rider findById(int id) {
        return findRiderById(id);
    }

    public List<Rider> getAllRiders() {
        return riders;
    }

    public List<Rider> findAll() {
        return getAllRiders();
    }
}
