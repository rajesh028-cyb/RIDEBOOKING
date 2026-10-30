package com.ridebooking.cli;

import com.ridebooking.models.Payment;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.service.PaymentService;
import com.ridebooking.service.PaymentService.FareBreakdown;
import java.util.List;
import java.util.Scanner;

/**
 * Command-Line Interface (CLI) for the Fare and Payment Management module.
 * Provides interactive menus for fare estimation, surge pricing preview,
 * payment processing (Cash, UPI, Wallet), payment history, and rider validation.
 */
public class PaymentCLI {
    private final PaymentService paymentService;
    private final Scanner scanner;

    public PaymentCLI(PaymentService paymentService) {
        if (paymentService == null) {
            throw new IllegalArgumentException("PaymentService cannot be null.");
        }
        this.paymentService = paymentService;
        this.scanner = new Scanner(System.in);
    }

    public PaymentCLI() {
        this(new PaymentService(new PaymentRepository()));
    }

    /**
     * Entry point to launch the Fare and Payment Management CLI.
     */
    public void start() {
        int choice = -1;
        do {
            printHeader();
            System.out.println("1. Calculate Fare & Display Breakdown");
            System.out.println("2. Check Surge Pricing & View Surge Fare");
            System.out.println("3. Process Ride Payment (Cash / UPI / Wallet)");
            System.out.println("4. Process Ride Cancellation Fee (₹30 to Driver)");
            System.out.println("5. View Payment Status (by Payment ID / Trip ID)");
            System.out.println("6. View Payment History (All / Rider / Trip / Failed)");
            System.out.println("7. Validate Rider Payment Eligibility");
            System.out.println("8. Clear Outstanding Failed Payment");
            System.out.println("9. Manage Rider Wallet (Check Balance / Deposit)");
            System.out.println("0. Exit");
            System.out.println("==================================================================");

            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    calculateFareAndBreakdown();
                    break;
                case 2:
                    checkSurgePricing();
                    break;
                case 3:
                    processRidePayment();
                    break;
                case 4:
                    processCancellation();
                    break;
                case 5:
                    viewPaymentStatus();
                    break;
                case 6:
                    viewPaymentHistoryMenu();
                    break;
                case 7:
                    validateRiderEligibility();
                    break;
                case 8:
                    clearOutstandingPayment();
                    break;
                case 9:
                    manageRiderWallet();
                    break;
                case 0:
                    System.out.println("\nExiting Payment Management Module. Thank you!\n");
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please select an option between 0 and 9.");
            }
        } while (choice != 0);
    }

    // =========================================================================
    // 1. FARE CALCULATION & BREAKDOWN
    // =========================================================================

    private void calculateFareAndBreakdown() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("        FARE ESTIMATION & BREAKDOWN");
        System.out.println("--------------------------------------------------");
        String vehicleType = promptVehicleType();
        double distance = readPositiveDouble("Enter trip distance in km: ");
        int duration = readPositiveInt("Enter estimated trip duration in minutes: ");
        int pending = readNonNegativeInt("Enter current pending ride requests: ");
        int available = readNonNegativeInt("Enter current available drivers: ");

        try {
            FareBreakdown fb = paymentService.getFareBreakdown(vehicleType, distance, duration, pending, available);
            displayFareBreakdown(fb);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public void displayFareBreakdown(FareBreakdown fb) {
        System.out.println("\n==================================================");
        System.out.println("                 FARE BREAKDOWN                   ");
        System.out.println("==================================================");
        System.out.printf(" Vehicle Type          : %s (₹%.2f/km)%n", fb.getVehicleType(), fb.getRatePerKm());
        System.out.printf(" Distance              : %.2f km%n", fb.getDistanceKm());
        System.out.printf(" Duration              : %d minutes%n", fb.getTimeMinutes());
        System.out.println("--------------------------------------------------");
        System.out.printf(" Base Fare             : ₹%.2f%n", fb.getBaseFare());
        System.out.printf(" Distance Charge       : ₹%.2f (%.2f km × ₹%.2f)%n", fb.getDistanceCharge(), fb.getDistanceKm(), fb.getRatePerKm());
        System.out.printf(" Time Charge           : ₹%.2f (%d mins × ₹1.00)%n", fb.getTimeCharge(), fb.getTimeMinutes());
        System.out.println("--------------------------------------------------");
        System.out.printf(" Calculated Subtotal   : ₹%.2f%n", fb.getSubtotal());
        if (fb.isMinimumFareApplied()) {
            System.out.printf(" Minimum Fare Applied  : YES (Raised to ₹%.2f)%n", fb.getMinimumFare());
        } else {
            System.out.printf(" Minimum Fare (₹50)    : Satisfied%n");
        }
        System.out.printf(" Standard Fare         : ₹%.2f%n", fb.getStandardFare());
        System.out.println("--------------------------------------------------");
        System.out.printf(" Pending Requests      : %d%n", fb.getPendingRequests());
        System.out.printf(" Available Drivers     : %d (Threshold: 2 × %d = %d)%n",
            fb.getAvailableDrivers(), fb.getAvailableDrivers(), 2 * fb.getAvailableDrivers());

        if (fb.isSurgeActive()) {
            System.out.printf(" Surge Status          : ACTIVE (1.5x Multiplier Applied)%n");
            System.out.printf(" Surge Fare            : ₹%.2f%n", fb.getFinalFare());
        } else {
            System.out.println(" Surge Status          : NORMAL (1.0x - No Surge)");
        }
        System.out.println("==================================================");
        System.out.printf(" TOTAL ESTIMATED FARE  : ₹%.2f%n", fb.getFinalFare());
        System.out.println("==================================================");
    }

    // =========================================================================
    // 2. SURGE PRICING CHECK
    // =========================================================================

    private void checkSurgePricing() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("          SURGE PRICING CALCULATOR");
        System.out.println("--------------------------------------------------");
        int pending = readNonNegativeInt("Enter current pending ride requests: ");
        int available = readNonNegativeInt("Enter available drivers count: ");
        double regularFare = readPositiveDouble("Enter standard base fare: ₹");

        boolean surgeActive = paymentService.isSurgeActive(pending, available);
        double multiplier = paymentService.getSurgeMultiplier(pending, available);
        double surgeFare = paymentService.calculateSurgeFare(regularFare);

        System.out.println("\n==================================================");
        System.out.println("             SURGE PRICING PREVIEW                ");
        System.out.println("==================================================");
        System.out.printf(" Pending Ride Requests : %d%n", pending);
        System.out.printf(" Available Drivers     : %d%n", available);
        System.out.printf(" Surge Threshold       : %d (2 × Available Drivers)%n", 2 * available);
        System.out.printf(" Condition Met?        : %s (%d %s %d)%n",
            surgeActive ? "YES -> SURGE ACTIVE" : "NO -> REGULAR PRICING",
            pending, surgeActive ? ">" : "<=", 2 * available);
        System.out.printf(" Multiplier            : %.1fx%n", multiplier);
        System.out.println("--------------------------------------------------");
        System.out.printf(" Regular Fare          : ₹%.2f%n", regularFare);
        if (surgeActive) {
            System.out.printf(" SURGE FARE (Before Confirmation): ₹%.2f%n", surgeFare);
            System.out.println(" [!] Surge fare is displayed before rider confirms ride.");
        } else {
            System.out.printf(" Final Fare            : ₹%.2f (Normal)%n", regularFare);
        }
        System.out.println("==================================================");
    }

    // =========================================================================
    // 3. PAYMENT PROCESSING
    // =========================================================================

    private void processRidePayment() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("            PROCESS RIDE PAYMENT");
        System.out.println("--------------------------------------------------");
        int tripId = readPositiveInt("Enter Trip ID: ");
        int riderId = readPositiveInt("Enter Rider ID: ");
        int driverId = readPositiveInt("Enter Driver ID: ");
        double amount = readPositiveDouble("Enter Fare Amount to pay: ₹");

        System.out.print("Is the trip completed? (1 for Yes, 2 for No/In-Progress): ");
        int tripCompletedChoice = readInt("");
        boolean isTripCompleted = (tripCompletedChoice == 1);

        String paymentMethod = promptPaymentMethod();

        try {
            Payment payment = paymentService.processPayment(tripId, riderId, driverId, amount, paymentMethod, isTripCompleted);
            displayPaymentReceipt(payment);
        } catch (IllegalStateException e) {
            System.out.println("\n[VALIDATION FAILED] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        }
    }

    private void displayPaymentReceipt(Payment payment) {
        System.out.println("\n==================================================");
        System.out.println("                 PAYMENT RECEIPT                  ");
        System.out.println("==================================================");
        System.out.printf(" Payment ID        : %d%n", payment.getPaymentId());
        System.out.printf(" Trip ID           : %d%n", payment.getTripId());
        System.out.printf(" Rider ID          : %d%n", payment.getRiderId());
        System.out.printf(" Driver ID         : %d%n", payment.getDriverId());
        System.out.printf(" Payment Amount    : ₹%.2f%n", payment.getAmount());
        System.out.printf(" Payment Date      : %s%n", payment.getFormattedPaymentDate());
        System.out.printf(" Payment Method    : %s%n", payment.getPaymentMethod());

        if (payment.isPaid()) {
            System.out.println(" Payment Status    : PAID [SUCCESS]");
            System.out.println("--------------------------------------------------");
            System.out.println(" REVENUE DISTRIBUTION (80% Driver / 20% Platform):");
            System.out.printf("   - Driver Earnings   : ₹%.2f (80%%)%n", payment.getDriverEarnings());
            System.out.printf("   - Platform Earnings : ₹%.2f (20%%)%n", payment.getPlatformEarnings());
        } else if (payment.isPending()) {
            System.out.println(" Payment Status    : PENDING (Cash payment pending trip completion)");
            System.out.println("--------------------------------------------------");
            System.out.println(" Potential Distribution Upon Completion:");
            System.out.printf("   - Driver Earnings   : ₹%.2f (80%%)%n", payment.getDriverEarnings());
            System.out.printf("   - Platform Earnings : ₹%.2f (20%%)%n", payment.getPlatformEarnings());
        } else {
            System.out.println(" Payment Status    : FAILED [INSUFFICIENT FUNDS / ERROR]");
            System.out.println("--------------------------------------------------");
            System.out.println(" [!] WARNING: Outstanding failed payment recorded.");
            System.out.printf(" Rider %d is blocked from requesting rides until cleared.%n", payment.getRiderId());
        }
        System.out.println("==================================================");
    }

    // =========================================================================
    // 4. CANCELLATION FEE
    // =========================================================================

    private void processCancellation() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("        PROCESS CANCELLATION FEE");
        System.out.println("--------------------------------------------------");
        System.out.println("Cancellation Fee: ₹30.00 (Goes 100% to Driver)");
        int tripId = readPositiveInt("Enter Trip ID: ");
        int riderId = readPositiveInt("Enter Rider ID: ");
        int driverId = readPositiveInt("Enter Driver ID: ");
        String method = promptPaymentMethod();

        try {
            Payment payment = paymentService.processCancellationFee(tripId, riderId, driverId, method);
            System.out.println("\n[SUCCESS] Cancellation fee recorded successfully.");
            displayPaymentReceipt(payment);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. VIEW PAYMENT STATUS
    // =========================================================================

    private void viewPaymentStatus() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("              VIEW PAYMENT STATUS");
        System.out.println("--------------------------------------------------");
        System.out.println("1. Search by Payment ID");
        System.out.println("2. Search by Trip ID");
        int searchChoice = readInt("Enter choice (1 or 2): ");

        Payment payment = null;
        if (searchChoice == 1) {
            int paymentId = readPositiveInt("Enter Payment ID: ");
            payment = paymentService.getPaymentById(paymentId);
        } else if (searchChoice == 2) {
            int tripId = readPositiveInt("Enter Trip ID: ");
            payment = paymentService.getPaymentByTripId(tripId);
        } else {
            System.out.println("[!] Invalid search choice.");
            return;
        }

        if (payment == null) {
            System.out.println("\n[-] No payment record found matching your query.");
        } else {
            displayPaymentReceipt(payment);
        }
    }

    // =========================================================================
    // 6. PAYMENT HISTORY
    // =========================================================================

    private void viewPaymentHistoryMenu() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("             VIEW PAYMENT HISTORY");
        System.out.println("--------------------------------------------------");
        System.out.println("1. View All Payments");
        System.out.println("2. View Payments by Rider ID");
        System.out.println("3. View Payments by Trip ID");
        System.out.println("4. View All Failed Payments");
        int choice = readInt("Enter choice (1-4): ");

        List<Payment> list;
        switch (choice) {
            case 1:
                list = paymentService.getAllPayments();
                break;
            case 2:
                int riderId = readPositiveInt("Enter Rider ID: ");
                list = paymentService.getPaymentsByRiderId(riderId);
                break;
            case 3:
                int tripId = readPositiveInt("Enter Trip ID: ");
                list = paymentService.getPaymentsByTripId(tripId);
                break;
            case 4:
                list = paymentService.getFailedPayments();
                break;
            default:
                System.out.println("[!] Invalid choice.");
                return;
        }

        renderPaymentsTable(list);
    }

    private void renderPaymentsTable(List<Payment> list) {
        if (list.isEmpty()) {
            System.out.println("\n[-] No payment records found.");
            return;
        }

        System.out.println("\n+------------+---------+----------+----------+-----------+---------+---------+---------------------+-----------+------------+");
        System.out.println("| Payment ID | Trip ID | Rider ID | Driver ID| Amount    | Method  | Status  | Date & Time         | Driver ₹  | Platform ₹ |");
        System.out.println("+------------+---------+----------+----------+-----------+---------+---------+---------------------+-----------+------------+");

        for (Payment p : list) {
            System.out.printf("| %-10d | %-7d | %-8d | %-8d | ₹%-8.2f | %-7s | %-7s | %-19s | ₹%-8.2f | ₹%-9.2f |%n",
                p.getPaymentId(), p.getTripId(), p.getRiderId(), p.getDriverId(),
                p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus(),
                p.getFormattedPaymentDate(), p.getDriverEarnings(), p.getPlatformEarnings()
            );
        }
        System.out.println("+------------+---------+----------+----------+-----------+---------+---------+---------------------+-----------+------------+");
        System.out.printf("Total Records: %d%n%n", list.size());
    }

    // =========================================================================
    // 7. RIDER ELIGIBILITY VALIDATION
    // =========================================================================

    private void validateRiderEligibility() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("         VALIDATE RIDER PAYMENT ELIGIBILITY");
        System.out.println("--------------------------------------------------");
        int riderId = readPositiveInt("Enter Rider ID: ");

        boolean eligible = paymentService.canRiderRequestRide(riderId);
        List<Payment> failed = paymentService.getOutstandingFailedPayments(riderId);

        if (eligible) {
            System.out.println("\n[✓] ELIGIBLE: Rider " + riderId + " has no outstanding failed payments.");
            System.out.println("The rider is fully authorized to request new rides.");
        } else {
            System.out.println("\n[✗] BLOCKED: Rider " + riderId + " CANNOT request a ride!");
            System.out.println("Reason: Found " + failed.size() + " outstanding failed payment(s):");
            renderPaymentsTable(failed);
            System.out.println("Please clear outstanding payment using option 8.");
        }
    }

    // =========================================================================
    // 8. CLEAR OUTSTANDING FAILED PAYMENT
    // =========================================================================

    private void clearOutstandingPayment() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("          CLEAR OUTSTANDING FAILED PAYMENT");
        System.out.println("--------------------------------------------------");
        int paymentId = readPositiveInt("Enter Payment ID to clear: ");
        System.out.println("Select Payment Method to clear the outstanding balance:");
        String method = promptPaymentMethod();

        try {
            Payment cleared = paymentService.clearFailedPayment(paymentId, method);
            System.out.println("\n[SUCCESS] Outstanding payment cleared successfully! Rider is unblocked.");
            displayPaymentReceipt(cleared);
        } catch (Exception e) {
            System.out.println("\n[FAILED TO CLEAR] " + e.getMessage());
        }
    }

    // =========================================================================
    // 9. WALLET MANAGEMENT
    // =========================================================================

    private void manageRiderWallet() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("              RIDER WALLET MANAGEMENT");
        System.out.println("--------------------------------------------------");
        int riderId = readPositiveInt("Enter Rider ID: ");
        double currentBalance = paymentService.getWalletBalance(riderId);
        System.out.printf("Current Wallet Balance for Rider %d: ₹%.2f%n", riderId, currentBalance);

        System.out.println("1. Add / Recharge Wallet Funds");
        System.out.println("2. Return to Main Menu");
        int choice = readInt("Enter choice: ");

        if (choice == 1) {
            double deposit = readPositiveDouble("Enter amount to deposit: ₹");
            try {
                paymentService.addWalletBalance(riderId, deposit);
                System.out.printf("[SUCCESS] Added ₹%.2f. New Balance: ₹%.2f%n",
                    deposit, paymentService.getWalletBalance(riderId));
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // HELPERS & PROMPTS
    // =========================================================================

    private String promptVehicleType() {
        while (true) {
            System.out.println("Select Vehicle Type:");
            System.out.println("  1. Bike  (₹6/km)");
            System.out.println("  2. Auto  (₹10/km)");
            System.out.println("  3. Mini  (₹12/km)");
            System.out.println("  4. Sedan (₹15/km)");
            System.out.println("  5. SUV   (₹20/km)");
            int choice = readInt("Select (1-5): ");
            switch (choice) {
                case 1: return "Bike";
                case 2: return "Auto";
                case 3: return "Mini";
                case 4: return "Sedan";
                case 5: return "SUV";
                default:
                    System.out.println("[!] Invalid vehicle choice. Please enter 1-5.");
            }
        }
    }

    private String promptPaymentMethod() {
        while (true) {
            System.out.println("Select Payment Method:");
            System.out.println("  1. Cash   (Remains Pending until trip completion)");
            System.out.println("  2. UPI    (Instant digital payment)");
            System.out.println("  3. Wallet (Fails if balance is insufficient)");
            int choice = readInt("Select (1-3): ");
            switch (choice) {
                case 1: return Payment.METHOD_CASH;
                case 2: return Payment.METHOD_UPI;
                case 3: return Payment.METHOD_WALLET;
                default:
                    System.out.println("[!] Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void printHeader() {
        System.out.println("\n==================================================================");
        System.out.println("         FARE AND PAYMENT MANAGEMENT MODULE (com.ridebooking)    ");
        System.out.println("==================================================================");
    }

    private int readInt(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("[!] Please enter a valid integer: ");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int val = readInt(prompt);
            if (val > 0) return val;
            System.out.println("[!] Value must be greater than 0.");
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            int val = readInt(prompt);
            if (val >= 0) return val;
            System.out.println("[!] Value cannot be negative.");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val > 0) return val;
                System.out.println("[!] Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a valid decimal number.");
            }
        }
    }

    // =========================================================================
    // STANDALONE MAIN METHOD FOR TESTING & DEMONSTRATION
    // =========================================================================

    public static void main(String[] args) {
        PaymentRepository repository = new PaymentRepository();
        PaymentService service = new PaymentService(repository);

        // Preload sample test data for instant demonstration
        service.setWalletBalance(1, 500.0); // Rider 1 has ₹500 wallet balance
        service.setWalletBalance(2, 50.0);  // Rider 2 has ₹50 wallet balance (low balance)

        // Seed some sample payments
        // Trip 101: UPI payment of ₹240 completed
        service.processPayment(101, 1, 1, 240.0, Payment.METHOD_UPI, true);

        // Trip 102: Cash payment of ₹150 in-progress (Pending)
        service.processPayment(102, 3, 2, 150.0, Payment.METHOD_CASH, false);

        PaymentCLI cli = new PaymentCLI(service);
        cli.start();
    }
}
