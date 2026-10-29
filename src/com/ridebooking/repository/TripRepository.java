package com.ridebooking.repository;

import com.ridebooking.models.Trip;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    private List<Trip> trips = new ArrayList<>();

    public void save(Trip trip) {
        trips.add(trip);
    }

    public Trip findById(int id) {
        for (Trip trip : trips) {
            if (trip.getId() == id) {
                return trip;
            }
        }
        return null;
    }

    public List<Trip> findAll() {
        return trips;
    }
}
