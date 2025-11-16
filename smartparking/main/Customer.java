package main;

import util.Constants;

public class Customer extends User {
    private String[] vehiclePlates;
    private int vehicleCount;
    private Account account;

    public Customer(String username, String hashedPassword, String name, String email) {
        super(username, hashedPassword, name, email, Constants.ROLE_CUSTOMER);
        this.vehiclePlates = new String[5];
        this.vehicleCount = 0;
        this.account = new Account(username, 0.0);
    }

    public Customer(String username, String hashedPassword, String name, String email, double initialBalance) {
        super(username, hashedPassword, name, email, Constants.ROLE_CUSTOMER);
        this.vehiclePlates = new String[5];
        this.vehicleCount = 0;
        this.account = new Account(username, initialBalance);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Customer Dashboard for " + name + " ---");
        System.out.println("Registered vehicles:");
        for (int i = 0; i < vehicleCount; i++) {
            System.out.println((i + 1) + ". " + vehiclePlates[i]);
        }
        System.out.println("Account balance: " + account.getBalance());
    }

    public void addVehicle(String plate) {
        if (vehicleCount >= vehiclePlates.length) {
            throw new IllegalStateException("Max vehicles reached");
        }
        vehiclePlates[vehicleCount++] = plate;
    }

    public String[] getVehicles() {
        return vehiclePlates;
    }

    public Account getAccount() {
        return account;
    }
}