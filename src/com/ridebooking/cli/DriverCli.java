package com.ridebooking.cli;

import com.ridebooking.service.DriverService;
import java.util.Scanner;

public class DriverCli {

    private Scanner sc = new Scanner(System.in);
    private DriverService driverService;

    public DriverCli(DriverService driverService) {
        this.driverService = driverService;
    }

    public void registerDriver() {

        System.out.print("Enter Driver Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();

        try {
            driverService.registerDriver(name, phone);
            System.out.println("Driver registered successfully!");
        } 
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
