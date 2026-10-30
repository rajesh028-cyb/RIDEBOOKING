package com.ridebooking.repository;

import com.ridebooking.models.Payment;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing in-memory storage and retrieval of Payment entities.
 * Uses an ArrayList for persistent in-memory storage across transactions.
 */
public class PaymentRepository {
    private final List<Payment> payments = new ArrayList<>();

    /**
     * Stores a new payment record or updates an existing record with matching paymentId.
     *
     * @param payment The payment object to store.
     * @throws IllegalArgumentException if payment is null.
     */
    public void savePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment record cannot be null.");
        }
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentId() == payment.getPaymentId()) {
                payments.set(i, payment);
                return;
            }
        }
        payments.add(payment);
    }

    /**
     * Alias method for savePayment.
     */
    public void save(Payment payment) {
        savePayment(payment);
    }

    /**
     * Finds a payment record by its unique payment ID.
     *
     * @param paymentId The payment ID to search for.
     * @return Payment entity if found, null otherwise.
     */
    public Payment findPaymentById(int paymentId) {
        for (Payment payment : payments) {
            if (payment.getPaymentId() == paymentId) {
                return payment;
            }
        }
        return null;
    }

    public Payment findById(int paymentId) {
        return findPaymentById(paymentId);
    }

    /**
     * Finds all payment records associated with a specific rider ID.
     *
     * @param riderId The rider ID to search for.
     * @return List of matching Payment entities (empty list if none found).
     */
    public List<Payment> findPaymentsByRider(int riderId) {
        List<Payment> results = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getRiderId() == riderId) {
                results.add(payment);
            }
        }
        return results;
    }

    public List<Payment> findByRiderId(int riderId) {
        return findPaymentsByRider(riderId);
    }

    /**
     * Finds all payment records associated with a specific trip ID.
     *
     * @param tripId The trip ID to search for.
     * @return List of matching Payment entities (empty list if none found).
     */
    public List<Payment> findPaymentsByTrip(int tripId) {
        List<Payment> results = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getTripId() == tripId) {
                results.add(payment);
            }
        }
        return results;
    }

    /**
     * Retrieves the latest/single payment associated with a trip ID.
     *
     * @param tripId The trip ID to search for.
     * @return Most recent Payment entity for the trip, or null if not found.
     */
    public Payment findPaymentByTripId(int tripId) {
        List<Payment> list = findPaymentsByTrip(tripId);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * Updates the status of an existing payment and updates earnings accordingly.
     *
     * @param paymentId The ID of the payment to update.
     * @param newStatus The new payment status (e.g. Paid, Failed, Pending).
     * @return true if updated successfully, false if payment was not found.
     * @throws IllegalArgumentException if newStatus is empty or null.
     */
    public boolean updatePaymentStatus(int paymentId, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment status cannot be empty.");
        }
        Payment payment = findPaymentById(paymentId);
        if (payment != null) {
            payment.setPaymentStatus(newStatus);
            return true;
        }
        return false;
    }

    /**
     * Retrieves all failed payment records across the system.
     *
     * @return List of Payment entities with status "Failed".
     */
    public List<Payment> getFailedPayments() {
        List<Payment> failedList = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.isFailed()) {
                failedList.add(payment);
            }
        }
        return failedList;
    }

    public List<Payment> findFailedPayments() {
        return getFailedPayments();
    }

    /**
     * Retrieves all failed payment records for a specific rider.
     * Used to validate rider ride request eligibility.
     *
     * @param riderId The rider ID to check.
     * @return List of failed Payment entities for this rider.
     */
    public List<Payment> findFailedPaymentsByRider(int riderId) {
        List<Payment> riderFailed = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getRiderId() == riderId && payment.isFailed()) {
                riderFailed.add(payment);
            }
        }
        return riderFailed;
    }

    /**
     * Retrieves all stored payments.
     *
     * @return A copy of the list of all Payment entities.
     */
    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }

    public List<Payment> findAll() {
        return getAllPayments();
    }

    /**
     * Clears all payment records (useful for unit tests).
     */
    public void clear() {
        payments.clear();
    }
}
