package com.ridebooking.repository;

import com.ridebooking.models.Rider;
import java.util.ArrayList;
import java.util.List;

public class RiderRepository {
    private List<Rider> riders = new ArrayList<>();

    public void save(Rider rider) {
        riders.add(rider);
    }

    public Rider findById(int id) {
        for (Rider rider : riders) {
            if (rider.getId() == id) {
                return rider;
            }
        }
        return null;
    }

    public List<Rider> findAll() {
        return riders;
    }
}
