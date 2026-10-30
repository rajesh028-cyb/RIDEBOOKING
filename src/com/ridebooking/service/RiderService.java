package com.ridebooking.service;

import com.ridebooking.models.Rider;
import com.ridebooking.repository.RiderRepository;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business logic for rider registration, validation, and access control.
 */
public class RiderService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9][0-9\\- ]{6,14}$");

    private final RiderRepository riderRepository;
    private int nextRiderId = 1;

    public RiderService(RiderRepository riderRepository) {
        if (riderRepository == null) {
            throw new IllegalArgumentException("RiderRepository cannot be null.");
        }
        this.riderRepository = riderRepository;
    }

    public Rider registerRider(String name, String phoneNumber, String email) {
        validateName(name);
        validatePhone(phoneNumber);
        validateEmail(email);

        String trimmedName = name.trim();
        String trimmedPhone = phoneNumber.trim();
        String trimmedEmail = email.trim();

        if (!riderRepository.isPhoneNumberUnique(trimmedPhone)) {
            throw new IllegalArgumentException(
                    "A rider with this phone number already exists.");
        }

        Rider rider = new Rider(nextRiderId++, trimmedName, trimmedPhone, trimmedEmail);
        riderRepository.save(rider);
        return rider;
    }

    public Rider getRiderById(int riderId) {
        Rider rider = riderRepository.findById(riderId);
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found with ID: " + riderId);
        }
        return rider;
    }

    public Rider getRiderByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        Rider rider = riderRepository.findByPhoneNumber(phoneNumber.trim());
        if (rider == null) {
            throw new IllegalArgumentException(
                    "Rider not found with phone number: " + phoneNumber);
        }
        return rider;
    }

    public List<Rider> getAllRiders() {
        return riderRepository.findAll();
    }

    public Rider updateRider(Rider rider) {
        if (rider == null) {
            throw new IllegalArgumentException("Rider cannot be null.");
        }
        validateName(rider.getName());
        validatePhone(rider.getPhoneNumber());
        validateEmail(rider.getEmail());

        Rider existingByPhone = riderRepository.findByPhoneNumber(rider.getPhoneNumber());
        if (existingByPhone != null && existingByPhone.getRiderId() != rider.getRiderId()) {
            throw new IllegalArgumentException(
                    "A rider with this phone number already exists.");
        }

        if (!riderRepository.update(rider)) {
            throw new IllegalArgumentException(
                    "Rider not found with ID: " + rider.getRiderId());
        }
        return rider;
    }

    /**
     * Ensures a rider is allowed to request a ride.
     * Riders with failed payments cannot request another ride.
     */
    public void ensureCanRequestRide(int riderId) {
        Rider rider = getRiderById(riderId);
        if (rider.hasFailedPayment()) {
            throw new IllegalStateException(
                    "Rider has a failed payment and cannot request another ride.");
        }
    }

    public boolean canRequestRide(int riderId) {
        Rider rider = getRiderById(riderId);
        return !rider.hasFailedPayment();
    }

    public void markFailedPayment(int riderId, boolean failed) {
        Rider rider = getRiderById(riderId);
        rider.setFailedPayment(failed);
        riderRepository.update(rider);
    }

    public void flagForAdminReview(int riderId, boolean flagged) {
        Rider rider = getRiderById(riderId);
        rider.setFlaggedForAdminReview(flagged);
        riderRepository.update(rider);
    }

    public void updateAverageRating(int riderId, double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0.");
        }
        Rider rider = getRiderById(riderId);
        rider.setAverageRating(rating);
        riderRepository.update(rider);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be empty.");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Rider name must be at least 2 characters.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleanPhone = phone.trim();
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new IllegalArgumentException(
                    "Phone number must be 7–15 digits and may include +, spaces, or hyphens.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }
}
