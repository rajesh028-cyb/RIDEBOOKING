package com.ridebooking.service;

import com.ridebooking.models.Payment;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.repository.TripRepository;
import java.util.List;

public class PaymentService {
    private PaymentRepository paymentRepository;
    private TripRepository tripRepository;
    private int nextPaymentId = 1;

    public PaymentService(PaymentRepository paymentRepository, TripRepository tripRepository) {
        this.paymentRepository = paymentRepository;
        this.tripRepository = tripRepository;
    }

    public Trip getTripForPayment(int tripId) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"COMPLETED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Payment can only be made after trip completion. Current trip status: " + trip.getTripStatus());
        }

        Payment existingPayment = paymentRepository.findPaymentByTripId(tripId);
        if (existingPayment != null && "PAID".equals(existingPayment.getPaymentStatus())) {
            throw new IllegalStateException("Payment has already been made for this trip.");
        }

        return trip;
    }

    public Payment processPayment(int tripId, boolean confirmed) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"COMPLETED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Payment can only be made after trip completion. Current trip status: " + trip.getTripStatus());
        }

        Payment existingPayment = paymentRepository.findPaymentByTripId(tripId);
        if (existingPayment != null) {
            if ("PAID".equals(existingPayment.getPaymentStatus())) {
                throw new IllegalStateException("Payment has already been made for this trip.");
            }
            if (confirmed) {
                existingPayment.setPaymentStatus("PAID");
            } else {
                existingPayment.setPaymentStatus("PENDING");
            }
            return existingPayment;
        }

        Payment payment = new Payment(nextPaymentId++, tripId, trip.getFare(), confirmed ? "PAID" : "PENDING");
        paymentRepository.savePayment(payment);
        return payment;
    }

    public Payment getPaymentByTripId(int tripId) {
        return paymentRepository.findPaymentByTripId(tripId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }
}
