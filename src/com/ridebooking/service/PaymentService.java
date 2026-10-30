package com.ridebooking.service;

import com.ridebooking.models.Payment;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.repository.TripRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service handling Fare calculation, Surge pricing, Payment processing,
 * Revenue distribution, and Rider payment validation for the Ride Booking System.
 */
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;
    private final RiderService riderService;
    private int nextPaymentId = 1;

    // Pricing Constants
    public static final double BASE_FARE = 30.0;
    public static final double MINIMUM_FARE = 50.0;
    public static final double TIME_CHARGE_PER_MINUTE = 1.0;
    public static final double CANCELLATION_FEE = 30.0;
    public static final double SURGE_MULTIPLIER = 1.5;

    // Vehicle Type Rates per KM
    public static final double RATE_BIKE = 6.0;
    public static final double RATE_AUTO = 10.0;
    public static final double RATE_MINI = 12.0;
    public static final double RATE_SEDAN = 15.0;
    public static final double RATE_SUV = 20.0;

    // In-memory wallet storage for Riders (RiderId -> Balance)
    private final Map<Integer, Double> riderWallets = new HashMap<>();

    /**
     * Standalone constructor injecting only PaymentRepository.
     */
    public PaymentService(PaymentRepository paymentRepository) {
        this(paymentRepository, null, null);
    }

    /**
     * Constructor for integration with TripRepository.
     */
    public PaymentService(PaymentRepository paymentRepository, TripRepository tripRepository) {
        this(paymentRepository, tripRepository, null);
    }

    /**
     * Constructor for integration with RiderService.
     */
    public PaymentService(PaymentRepository paymentRepository, RiderService riderService) {
        this(paymentRepository, null, riderService);
    }

    /**
     * Full integration constructor injecting PaymentRepository, TripRepository, and RiderService.
     */
    public PaymentService(PaymentRepository paymentRepository, TripRepository tripRepository, RiderService riderService) {
        if (paymentRepository == null) {
            throw new IllegalArgumentException("PaymentRepository cannot be null.");
        }
        this.paymentRepository = paymentRepository;
        this.tripRepository = tripRepository;
        this.riderService = riderService;

        // Advance nextPaymentId past any existing records
        for (Payment p : paymentRepository.getAllPayments()) {
            if (p.getPaymentId() >= nextPaymentId) {
                nextPaymentId = p.getPaymentId() + 1;
            }
        }
    }

    // =========================================================================
    // 1. FARE CALCULATION
    // =========================================================================

    /**
     * Resolves the per-kilometer rate based on vehicle type.
     * Supported types: Bike (₹6), Auto (₹10), Mini (₹12), Sedan (₹15), SUV (₹20).
     */
    public double getRatePerKm(String vehicleType) {
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type cannot be empty.");
        }
        switch (vehicleType.trim().toUpperCase()) {
            case "BIKE":
                return RATE_BIKE;
            case "AUTO":
                return RATE_AUTO;
            case "MINI":
                return RATE_MINI;
            case "SEDAN":
                return RATE_SEDAN;
            case "SUV":
                return RATE_SUV;
            default:
                throw new IllegalArgumentException(
                    "Invalid vehicle type: '" + vehicleType + "'. Supported types: Bike, Auto, Mini, Sedan, SUV."
                );
        }
    }

    /**
     * Calculates the standard regular fare for a ride.
     * Formula: Base Fare (₹30) + (Distance × Rate/km) + (Time × ₹1/min).
     * Enforces Minimum Fare of ₹50.
     *
     * @param vehicleType   The type of vehicle (Bike, Auto, Mini, Sedan, SUV).
     * @param distanceInKm  The total trip distance in kilometers.
     * @param timeInMinutes The trip duration in minutes.
     * @return The calculated fare amount.
     */
    public double calculateFare(String vehicleType, double distanceInKm, int timeInMinutes) {
        return calculateFare(vehicleType, distanceInKm, timeInMinutes, false);
    }

    /**
     * Calculates ride fare with optional surge multiplier.
     *
     * @param vehicleType   The vehicle type.
     * @param distanceInKm  The trip distance in km.
     * @param timeInMinutes The duration in minutes.
     * @param applySurge    Whether to apply the 1.5x surge pricing multiplier.
     * @return The final fare amount rounded to 2 decimal places.
     */
    public double calculateFare(String vehicleType, double distanceInKm, int timeInMinutes, boolean applySurge) {
        if (distanceInKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        if (timeInMinutes < 0) {
            throw new IllegalArgumentException("Time in minutes cannot be negative.");
        }

        double ratePerKm = getRatePerKm(vehicleType);
        double distanceCharge = distanceInKm * ratePerKm;
        double timeCharge = timeInMinutes * TIME_CHARGE_PER_MINUTE;
        double subtotal = BASE_FARE + distanceCharge + timeCharge;

        // Apply Minimum Fare of ₹50
        double standardFare = Math.max(subtotal, MINIMUM_FARE);

        // Apply Surge Multiplier if active
        double finalFare = applySurge ? (standardFare * SURGE_MULTIPLIER) : standardFare;
        return Math.round(finalFare * 100.0) / 100.0;
    }

    /**
     * Calculates fare automatically detecting surge conditions.
     */
    public double calculateFare(String vehicleType, double distanceInKm, int timeInMinutes,
                                int pendingRequests, int availableDrivers) {
        boolean surge = isSurgeActive(pendingRequests, availableDrivers);
        return calculateFare(vehicleType, distanceInKm, timeInMinutes, surge);
    }

    /**
     * Integration helper: Uses Trip model to calculate fare and update trip fare.
     */
    public double calculateFare(Trip trip, String vehicleType, int timeInMinutes, boolean applySurge) {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null.");
        }
        double fare = calculateFare(vehicleType, trip.getDistance(), timeInMinutes, applySurge);
        trip.setFare(fare);
        return fare;
    }

    /**
     * Returns a detailed breakdown of fare calculations for display in CLI.
     */
    public FareBreakdown getFareBreakdown(String vehicleType, double distanceInKm, int timeInMinutes,
                                         int pendingRequests, int availableDrivers) {
        if (distanceInKm < 0 || timeInMinutes < 0) {
            throw new IllegalArgumentException("Distance and time cannot be negative.");
        }
        double ratePerKm = getRatePerKm(vehicleType);
        double distanceCharge = Math.round(distanceInKm * ratePerKm * 100.0) / 100.0;
        double timeCharge = Math.round(timeInMinutes * TIME_CHARGE_PER_MINUTE * 100.0) / 100.0;
        double subtotal = Math.round((BASE_FARE + distanceCharge + timeCharge) * 100.0) / 100.0;
        boolean minFareApplied = subtotal < MINIMUM_FARE;
        double standardFare = Math.max(subtotal, MINIMUM_FARE);
        boolean surgeActive = isSurgeActive(pendingRequests, availableDrivers);
        double finalFare = surgeActive ? Math.round(standardFare * SURGE_MULTIPLIER * 100.0) / 100.0 : standardFare;

        return new FareBreakdown(
            vehicleType, distanceInKm, timeInMinutes, BASE_FARE, ratePerKm,
            distanceCharge, timeCharge, subtotal, MINIMUM_FARE, minFareApplied,
            standardFare, surgeActive, SURGE_MULTIPLIER, finalFare,
            pendingRequests, availableDrivers
        );
    }

    // =========================================================================
    // 2. SURGE PRICING
    // =========================================================================

    /**
     * Determines whether surge pricing applies.
     * Rule: Apply 1.5x multiplier when pending requests exceed twice the number of available drivers.
     * (pendingRequests > 2 * availableDrivers)
     */
    public boolean isSurgeActive(int pendingRequests, int availableDrivers) {
        if (pendingRequests < 0 || availableDrivers < 0) {
            throw new IllegalArgumentException("Pending requests and available drivers must be non-negative.");
        }
        return pendingRequests > (2 * availableDrivers);
    }

    /**
     * Returns the surge multiplier (1.5x if surge active, 1.0x otherwise).
     */
    public double getSurgeMultiplier(int pendingRequests, int availableDrivers) {
        return isSurgeActive(pendingRequests, availableDrivers) ? SURGE_MULTIPLIER : 1.0;
    }

    /**
     * Calculates the surge fare given a regular fare.
     */
    public double calculateSurgeFare(double regularFare) {
        if (regularFare < 0) {
            throw new IllegalArgumentException("Regular fare cannot be negative.");
        }
        return Math.round(regularFare * SURGE_MULTIPLIER * 100.0) / 100.0;
    }

    // =========================================================================
    // 3. PAYMENT PROCESSING
    // =========================================================================

    /**
     * Processes payment for a ride using Cash, UPI, or Wallet.
     * - Wallet payment fails if balance is insufficient.
     * - Cash remains Pending until trip completion.
     * - Stores amount, date, method, status, and earnings.
     * - Validates that the rider has no prior outstanding failed payments.
     *
     * @param tripId          The trip ID.
     * @param riderId         The rider ID.
     * @param driverId        The driver ID.
     * @param amount          The payment amount.
     * @param paymentMethod   Payment method: Cash, UPI, Wallet.
     * @param isTripCompleted Whether the trip has completed.
     * @return Payment record created.
     */
    public Payment processPayment(int tripId, int riderId, int driverId, double amount,
                                  String paymentMethod, boolean isTripCompleted) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty.");
        }

        // Rider validation: Riders with failed payments cannot request/proceed until cleared
        validateRiderPaymentEligibility(riderId);

        String method = normalizePaymentMethod(paymentMethod);
        String status;
        double driverEarnings;
        double platformEarnings;

        switch (method) {
            case Payment.METHOD_WALLET:
                double currentWallet = getWalletBalance(riderId);
                if (currentWallet < amount) {
                    // Wallet payment FAILS
                    status = Payment.STATUS_FAILED;
                    driverEarnings = 0.0;
                    platformEarnings = 0.0;
                } else {
                    // Deduct wallet balance
                    riderWallets.put(riderId, Math.round((currentWallet - amount) * 100.0) / 100.0);
                    status = Payment.STATUS_PAID;
                    driverEarnings = Math.round(amount * 0.80 * 100.0) / 100.0;
                    platformEarnings = Math.round(amount * 0.20 * 100.0) / 100.0;
                }
                break;

            case Payment.METHOD_CASH:
                if (isTripCompleted) {
                    status = Payment.STATUS_PAID;
                } else {
                    // Cash remains Pending until trip completion
                    status = Payment.STATUS_PENDING;
                }
                driverEarnings = Math.round(amount * 0.80 * 100.0) / 100.0;
                platformEarnings = Math.round(amount * 0.20 * 100.0) / 100.0;
                break;

            case Payment.METHOD_UPI:
                status = Payment.STATUS_PAID;
                driverEarnings = Math.round(amount * 0.80 * 100.0) / 100.0;
                platformEarnings = Math.round(amount * 0.20 * 100.0) / 100.0;
                break;

            default:
                throw new IllegalArgumentException(
                    "Invalid payment method: '" + paymentMethod + "'. Supported methods: Cash, UPI, Wallet."
                );
        }

        Payment payment = new Payment(
            nextPaymentId++, tripId, riderId, driverId, amount,
            LocalDateTime.now(), method, status, driverEarnings, platformEarnings
        );

        paymentRepository.savePayment(payment);
        return payment;
    }

    /**
     * Completes a pending Cash payment when the trip reaches completion.
     */
    public Payment completeCashPayment(int paymentId) {
        Payment payment = paymentRepository.findPaymentById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment record not found with ID: " + paymentId);
        }
        if (!Payment.METHOD_CASH.equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new IllegalStateException("Only Cash payments can be finalized via completeCashPayment.");
        }
        if (payment.isPaid()) {
            return payment;
        }
        payment.setPaymentStatus(Payment.STATUS_PAID);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.savePayment(payment);
        return payment;
    }

    // =========================================================================
    // 4. REVENUE DISTRIBUTION & CANCELLATION FEE
    // =========================================================================

    /**
     * Calculates driver's 80% share.
     */
    public double calculateDriverEarnings(double amount) {
        return Math.round(amount * 0.80 * 100.0) / 100.0;
    }

    /**
     * Calculates platform's 20% share.
     */
    public double calculatePlatformEarnings(double amount) {
        return Math.round(amount * 0.20 * 100.0) / 100.0;
    }

    /**
     * Processes cancellation fee of ₹30, which goes entirely to driver (Driver: ₹30, Platform: ₹0).
     */
    public Payment processCancellationFee(int tripId, int riderId, int driverId, String paymentMethod) {
        String method = normalizePaymentMethod(paymentMethod);
        double amount = CANCELLATION_FEE;
        double driverEarnings = CANCELLATION_FEE;
        double platformEarnings = 0.0;
        String status = Payment.STATUS_PAID;

        if (Payment.METHOD_WALLET.equalsIgnoreCase(method)) {
            double currentWallet = getWalletBalance(riderId);
            if (currentWallet < amount) {
                status = Payment.STATUS_FAILED;
                driverEarnings = 0.0;
            } else {
                riderWallets.put(riderId, Math.round((currentWallet - amount) * 100.0) / 100.0);
            }
        }

        Payment payment = new Payment(
            nextPaymentId++, tripId, riderId, driverId, amount,
            LocalDateTime.now(), method, status, driverEarnings, platformEarnings
        );

        paymentRepository.savePayment(payment);
        return payment;
    }

    // =========================================================================
    // 5. PAYMENT VALIDATION & CLEARING
    // =========================================================================

    /**
     * Validates whether a rider can request a ride.
     * Rule: Riders with Failed payments cannot request another ride until outstanding payment is cleared.
     *
     * @param riderId The rider ID to validate.
     * @return true if the rider has no failed payments, false otherwise.
     */
    public boolean canRiderRequestRide(int riderId) {
        List<Payment> failed = paymentRepository.findFailedPaymentsByRider(riderId);
        return failed.isEmpty();
    }

    /**
     * Throws an IllegalStateException if the rider has outstanding failed payments.
     */
    public void validateRiderPaymentEligibility(int riderId) {
        List<Payment> failed = paymentRepository.findFailedPaymentsByRider(riderId);
        if (!failed.isEmpty()) {
            double totalOutstanding = 0.0;
            for (Payment p : failed) {
                totalOutstanding += p.getAmount();
            }
            throw new IllegalStateException(String.format(
                "Rider ID %d is blocked from requesting rides due to %d outstanding failed payment(s) totaling ₹%.2f. Please clear outstanding payments first.",
                riderId, failed.size(), totalOutstanding
            ));
        }
    }

    /**
     * Retrieves all outstanding failed payments for a rider.
     */
    public List<Payment> getOutstandingFailedPayments(int riderId) {
        return paymentRepository.findFailedPaymentsByRider(riderId);
    }

    /**
     * Clears an outstanding failed payment by retrying with Cash, UPI, or Wallet.
     * Once paid, the rider's ride request eligibility is restored.
     */
    public Payment clearFailedPayment(int paymentId, String paymentMethod) {
        Payment payment = paymentRepository.findPaymentById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment record not found with ID: " + paymentId);
        }
        if (!payment.isFailed()) {
            throw new IllegalStateException("Payment ID " + paymentId + " is not in Failed status (Current: " + payment.getPaymentStatus() + ").");
        }

        String method = normalizePaymentMethod(paymentMethod);
        if (Payment.METHOD_WALLET.equalsIgnoreCase(method)) {
            double currentWallet = getWalletBalance(payment.getRiderId());
            if (currentWallet < payment.getAmount()) {
                throw modernWalletException(payment.getRiderId(), currentWallet, payment.getAmount());
            }
            riderWallets.put(payment.getRiderId(), Math.round((currentWallet - payment.getAmount()) * 100.0) / 100.0);
        }

        payment.setPaymentMethod(method);
        payment.setPaymentStatus(Payment.STATUS_PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setDriverEarnings(Math.round(payment.getAmount() * 0.80 * 100.0) / 100.0);
        payment.setPlatformEarnings(Math.round(payment.getAmount() * 0.20 * 100.0) / 100.0);
        paymentRepository.savePayment(payment);
        return payment;
    }

    private IllegalStateException modernWalletException(int riderId, double current, double required) {
        return new IllegalStateException(String.format(
            "Insufficient wallet balance for Rider %d! Current balance is ₹%.2f, but required amount is ₹%.2f. Please recharge wallet or use UPI/Cash.",
            riderId, current, required
        ));
    }

    // =========================================================================
    // 6. WALLET MANAGEMENT
    // =========================================================================

    public double getWalletBalance(int riderId) {
        return riderWallets.getOrDefault(riderId, 0.0);
    }

    public void addWalletBalance(int riderId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        double current = getWalletBalance(riderId);
        riderWallets.put(riderId, Math.round((current + amount) * 100.0) / 100.0);
    }

    public void setWalletBalance(int riderId, double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Wallet balance cannot be negative.");
        }
        riderWallets.put(riderId, Math.round(balance * 100.0) / 100.0);
    }

    // =========================================================================
    // 7. LOOKUP & INTEGRATION METHODS
    // =========================================================================

    /**
     * Retrieves the trip and validates it for payment eligibility.
     */
    public Trip getTripForPayment(int tripId) {
        if (tripRepository == null) {
            throw new IllegalStateException("TripRepository is not configured.");
        }
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }
        if (!"COMPLETED".equalsIgnoreCase(trip.getTripStatus())) {
            throw new IllegalStateException("Payment can only be made after trip completion. Current trip status: " + trip.getTripStatus());
        }
        Payment existing = paymentRepository.findPaymentByTripId(tripId);
        if (existing != null && existing.isPaid()) {
            throw new IllegalStateException("Payment has already been made for this trip.");
        }
        return trip;
    }

    /**
     * Backward-compatible processPayment method for RideMenu integration.
     */
    public Payment processPayment(int tripId, boolean confirmed) {
        if (tripRepository != null) {
            Trip trip = tripRepository.findTripById(tripId);
            if (trip != null) {
                int riderId = (trip.getRider() != null) ? trip.getRider().getRiderId() : 0;
                int driverId = (trip.getDriver() != null) ? trip.getDriver().getDriverId() : 0;
                double amount = trip.getFare();
                Payment existing = paymentRepository.findPaymentByTripId(tripId);
                if (existing != null) {
                    if (existing.isPaid()) {
                        throw new IllegalStateException("Payment has already been made for Trip ID: " + tripId);
                    }
                    existing.setPaymentStatus(confirmed ? Payment.STATUS_PAID : Payment.STATUS_PENDING);
                    paymentRepository.savePayment(existing);
                    return existing;
                }
                Payment payment = new Payment(
                    nextPaymentId++, tripId, riderId, driverId, amount,
                    Payment.METHOD_CASH, confirmed ? Payment.STATUS_PAID : Payment.STATUS_PENDING
                );
                paymentRepository.savePayment(payment);
                return payment;
            }
        }

        Payment existing = paymentRepository.findPaymentByTripId(tripId);
        if (existing != null) {
            if (existing.isPaid()) {
                throw new IllegalStateException("Payment has already been made for Trip ID: " + tripId);
            }
            existing.setPaymentStatus(confirmed ? Payment.STATUS_PAID : Payment.STATUS_PENDING);
            paymentRepository.savePayment(existing);
            return existing;
        }

        Payment payment = new Payment(
            nextPaymentId++, tripId, 0, 0, 50.0,
            Payment.METHOD_CASH, confirmed ? Payment.STATUS_PAID : Payment.STATUS_PENDING
        );
        paymentRepository.savePayment(payment);
        return payment;
    }

    public Payment getPaymentById(int paymentId) {
        return paymentRepository.findPaymentById(paymentId);
    }

    public Payment getPaymentByTripId(int tripId) {
        return paymentRepository.findPaymentByTripId(tripId);
    }

    public List<Payment> getPaymentsByRiderId(int riderId) {
        return paymentRepository.findPaymentsByRider(riderId);
    }

    public List<Payment> getPaymentsByTripId(int tripId) {
        return paymentRepository.findPaymentsByTrip(tripId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

    public List<Payment> getFailedPayments() {
        return paymentRepository.getFailedPayments();
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public TripRepository getTripRepository() {
        return tripRepository;
    }

    public RiderService getRiderService() {
        return riderService;
    }

    private String normalizePaymentMethod(String method) {
        if (method == null) return Payment.METHOD_CASH;
        String m = method.trim();
        if (m.equalsIgnoreCase("cash")) return Payment.METHOD_CASH;
        if (m.equalsIgnoreCase("upi")) return Payment.METHOD_UPI;
        if (m.equalsIgnoreCase("wallet")) return Payment.METHOD_WALLET;
        throw new IllegalArgumentException(
            "Unsupported payment method: '" + method + "'. Allowed methods: Cash, UPI, Wallet."
        );
    }

    // =========================================================================
    // FARE BREAKDOWN VALUE OBJECT
    // =========================================================================

    public static class FareBreakdown {
        private final String vehicleType;
        private final double distanceKm;
        private final int timeMinutes;
        private final double baseFare;
        private final double ratePerKm;
        private final double distanceCharge;
        private final double timeCharge;
        private final double subtotal;
        private final double minimumFare;
        private final boolean minimumFareApplied;
        private final double standardFare;
        private final boolean surgeActive;
        private final double surgeMultiplier;
        private final double finalFare;
        private final int pendingRequests;
        private final int availableDrivers;

        public FareBreakdown(String vehicleType, double distanceKm, int timeMinutes,
                             double baseFare, double ratePerKm, double distanceCharge,
                             double timeCharge, double subtotal, double minimumFare,
                             boolean minimumFareApplied, double standardFare,
                             boolean surgeActive, double surgeMultiplier, double finalFare,
                             int pendingRequests, int availableDrivers) {
            this.vehicleType = vehicleType;
            this.distanceKm = distanceKm;
            this.timeMinutes = timeMinutes;
            this.baseFare = baseFare;
            this.ratePerKm = ratePerKm;
            this.distanceCharge = distanceCharge;
            this.timeCharge = timeCharge;
            this.subtotal = subtotal;
            this.minimumFare = minimumFare;
            this.minimumFareApplied = minimumFareApplied;
            this.standardFare = standardFare;
            this.surgeActive = surgeActive;
            this.surgeMultiplier = surgeMultiplier;
            this.finalFare = finalFare;
            this.pendingRequests = pendingRequests;
            this.availableDrivers = availableDrivers;
        }

        public String getVehicleType() { return vehicleType; }
        public double getDistanceKm() { return distanceKm; }
        public int getTimeMinutes() { return timeMinutes; }
        public double getBaseFare() { return baseFare; }
        public double getRatePerKm() { return ratePerKm; }
        public double getDistanceCharge() { return distanceCharge; }
        public double getTimeCharge() { return timeCharge; }
        public double getSubtotal() { return subtotal; }
        public double getMinimumFare() { return minimumFare; }
        public boolean isMinimumFareApplied() { return minimumFareApplied; }
        public double getStandardFare() { return standardFare; }
        public boolean isSurgeActive() { return surgeActive; }
        public double getSurgeMultiplier() { return surgeMultiplier; }
        public double getFinalFare() { return finalFare; }
        public int getPendingRequests() { return pendingRequests; }
        public int getAvailableDrivers() { return availableDrivers; }
    }
}
