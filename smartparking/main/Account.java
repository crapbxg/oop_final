package main;

import exceptions.InsufficientFundsException;
import util.PaymentGateway;

public class Account implements PaymentGateway {
    private Double balance;
    private String ownerUsername;

    public Account(String ownerUsername, double initialBalance) {
        this.ownerUsername = ownerUsername;
        this.balance = Double.valueOf(initialBalance);
    }

    public Account(String ownerUsername) {
        this(ownerUsername, 0.0);
    }

    public void deposit(double amount) {
        balance = Double.valueOf(balance.doubleValue() + amount);
        System.out.println("[Account] " + ownerUsername + " deposited " + amount);
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (balance.doubleValue() < amount) {
            throw new InsufficientFundsException("Insufficient funds for user: " + ownerUsername);
        }
        balance = Double.valueOf(balance.doubleValue() - amount);
    }

    @Override
    public boolean processPayment(String username, double amount) {
        if (!username.equals(ownerUsername)) {
            System.out.println("Wrong account user: " + username);
            return false;
        }
        try {
            withdraw(amount);
            System.out.println("[Account] " + ownerUsername + " paid " + amount);
            return true;
        } catch (InsufficientFundsException e) {
            return false;
        }
    }

    public Double getBalance() {
        return balance;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }
}