package com.ridebooking.cli;

import com.ridebooking.models.Rider;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.service.RiderService;
import java.util.Scanner;

/**
 * Command-line interface for rider registration and viewing rider details.
 */
public class RiderCLI {
    private final RiderService riderService;
    private final Scanner scanner;

    public RiderCLI(RiderService riderService) {
        if (riderService == null) {
            throw new IllegalArgumentException("RiderService cannot be null.");
        }
        this.riderService = riderService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    registerRider();
                    break;
                case "2":
                    viewRiderDetails();
                    break;
                case "3":
                    running = false;
                    System.out.println("Exiting Rider Management.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("===== Rider Management =====");
        System.out.println("1. Register Rider");
        System.out.println("2. View Rider Details");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");
    }

    private void registerRider() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            Rider rider = riderService.registerRider(name, phone, email);
            System.out.println("Rider registered successfully.");
            displayRider(rider);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void viewRiderDetails() {
        try {
            System.out.print("Enter rider ID: ");
            String input = scanner.nextLine().trim();
            int riderId = Integer.parseInt(input);
            Rider rider = riderService.getRiderById(riderId);
            displayRider(rider);
        } catch (NumberFormatException e) {
            System.out.println("Invalid rider ID. Please enter a number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Lookup failed: " + e.getMessage());
        }
    }

    public void displayRider(Rider rider) {
        if (rider == null) {
            System.out.println("No rider to display.");
            return;
        }
        System.out.println("--- Rider Information ---");
        System.out.println("ID:              " + rider.getRiderId());
        System.out.println("Name:            " + rider.getName());
        System.out.println("Phone:           " + rider.getPhoneNumber());
        System.out.println("Email:           " + rider.getEmail());
        System.out.println("Average Rating:  " + rider.getAverageRating());
        System.out.println("Failed Payment:  " + rider.hasFailedPayment());
        System.out.println("Admin Review:    " + rider.isFlaggedForAdminReview());
        System.out.println("-------------------------");
    }

    public static void main(String[] args) {
        RiderRepository repository = new RiderRepository();
        RiderService service = new RiderService(repository);
        RiderCLI cli = new RiderCLI(service);
        cli.start();
    }
}
