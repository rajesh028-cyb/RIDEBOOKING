package com.ridebooking.repository;

import com.ridebooking.models.Payment;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {
    private List<Payment> payments = new ArrayList<>();

    public void savePayment(Payment payment) {
        payments.add(payment);
    }

    public void save(Payment payment) {
        savePayment(payment);
    }

    public Payment findPaymentByTripId(int tripId) {
        for (Payment payment : payments) {
            if (payment.getTripId() == tripId) {
                return payment;
            }
        }
        return null;
    }

    public Payment findPaymentById(int paymentId) {
        for (Payment payment : payments) {
            if (payment.getPaymentId() == paymentId) {
                return payment;
            }
        }
        return null;
    }

    public List<Payment> getAllPayments() {
        return payments;
    }

    public List<Payment> findAll() {
        return getAllPayments();
    }
}
