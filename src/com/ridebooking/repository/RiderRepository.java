package com.ridebooking.repository;

import com.ridebooking.models.Rider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory storage and retrieval of riders.
 */
public class RiderRepository {
    private final List<Rider> riders = new ArrayList<>();

    public void save(Rider rider) {
        if (rider == null) {
            throw new IllegalArgumentException("Rider cannot be null.");
        }
        riders.add(rider);
    }

    public Rider findById(int riderId) {
        for (Rider rider : riders) {
            if (rider.getRiderId() == riderId) {
                return rider;
            }
        }
        return null;
    }

    public Rider findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String normalized = phoneNumber.trim();
        for (Rider rider : riders) {
            if (rider.getPhoneNumber() != null
                    && rider.getPhoneNumber().equalsIgnoreCase(normalized)) {
                return rider;
            }
        }
        return null;
    }

    public boolean isPhoneNumberUnique(String phoneNumber) {
        return findByPhoneNumber(phoneNumber) == null;
    }

    public boolean update(Rider updatedRider) {
        if (updatedRider == null) {
            throw new IllegalArgumentException("Rider cannot be null.");
        }
        for (int i = 0; i < riders.size(); i++) {
            if (riders.get(i).getRiderId() == updatedRider.getRiderId()) {
                riders.set(i, updatedRider);
                return true;
            }
        }
        return false;
    }

    public List<Rider> findAll() {
        return Collections.unmodifiableList(riders);
    }

    public int count() {
        return riders.size();
    }
}
