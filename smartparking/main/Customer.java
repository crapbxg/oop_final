package main;

import util.Constants;
import java.util.Arrays; // Import Arrays

public class Customer extends User {
    private String[] vehiclePlates;
    private int vehicleCount;
    private Account account;

    public Customer(String username, String hashedPassword, String name, String email) {
        // ... (constructor unchanged) ...
        super(username, hashedPassword, name, email, Constants.ROLE_CUSTOMER);
        this.vehiclePlates = new String[5];
        this.vehicleCount = 0;
        this.account = new Account(username, 0.0);
    }

    public Customer(String username, String hashedPassword, String name, String email, double initialBalance) {
        // ... (constructor unchanged) ...
        super(username, hashedPassword, name, email, Constants.ROLE_CUSTOMER);
        this.vehiclePlates = new String[5];
        this.vehicleCount = 0;
        this.account = new Account(username, initialBalance);
    }

    @Override
    public void showDashboard() {
        // ... (showDashboard method unchanged) ...
        System.out.println("--- Customer Dashboard for " + name + " ---");
        System.out.println("Registered vehicles:");
        for (int i = 0; i < vehicleCount; i++) {
            System.out.println((i + 1) + ". " + vehiclePlates[i]);
        }
        System.out.println("Account balance: " + account.getBalance());
    }

    public void addVehicle(String plate) {
        // ... (addVehicle method unchanged) ...
        if (vehicleCount >= vehiclePlates.length) {
            throw new IllegalStateException("Max vehicles reached");
        }
        vehiclePlates[vehicleCount++] = plate;
    }

    // MODIFIED - Renamed and returns a clean copy
    public String[] getVehiclePlates() {
        return Arrays.copyOf(vehiclePlates, vehicleCount);
    }

    public Account getAccount() {
        // ... (getAccount method unchanged) ...
        return account;
    }
}