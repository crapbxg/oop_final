package service;

import main.Customer;

public class ExitGate {
    private ParkingSystem system;

    public ExitGate(ParkingSystem system) {
        this.system = system;
    }

    // Takes Customer to handle payment
    public Double processExit(String ticketId, Customer customer) throws Exception {
        return system.markExit(ticketId, customer);
    }
}