package com.ridebooking.service;

import com.ridebooking.models.Rider;
import com.ridebooking.repository.RiderRepository;
import java.util.List;

public class RiderService {
    private RiderRepository riderRepository;
    private int nextRiderId = 1;

    public RiderService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    public Rider registerRider(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be empty.");
        }
        validatePhone(phone);

        Rider rider = new Rider(nextRiderId++, name.trim(), phone.trim());
        riderRepository.saveRider(rider);
        return rider;
    }

    public Rider getRiderById(int id) {
        return riderRepository.findRiderById(id);
    }

    public List<Rider> getAllRiders() {
        return riderRepository.getAllRiders();
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleanPhone = phone.trim();
        if (cleanPhone.length() < 7 || cleanPhone.length() > 15) {
            throw new IllegalArgumentException("Phone number must have between 7 and 15 digits.");
        }
        for (int i = 0; i < cleanPhone.length(); i++) {
            char c = cleanPhone.charAt(i);
            if (!Character.isDigit(c) && c != '+' && c != '-') {
                throw new IllegalArgumentException("Phone number contains invalid characters.");
            }
        }
    }
}
