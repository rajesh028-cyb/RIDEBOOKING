package com.ridebooking.repository;

import com.ridebooking.models.Trip;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    private List<Trip> trips = new ArrayList<>();

    public void saveTrip(Trip trip) {
        trips.add(trip);
    }

    public void save(Trip trip) {
        saveTrip(trip);
    }

    public Trip findTripById(int id) {
        for (Trip trip : trips) {
            if (trip.getTripId() == id) {
                return trip;
            }
        }
        return null;
    }

    public Trip findById(int id) {
        return findTripById(id);
    }

    public List<Trip> getAllTrips() {
        return trips;
    }

    public List<Trip> findAll() {
        return getAllTrips();
    }
}
