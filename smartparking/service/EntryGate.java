package service;

import main.ParkingTicket;

public class EntryGate {
    private ParkingSystem system;

    public EntryGate(ParkingSystem system) {
        this.system = system;
    }

    public ParkingTicket processEntry(String username, String vehicleLicense) {
        return system.markEntry(username, vehicleLicense);
    }
}