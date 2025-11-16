package service;

import main.ParkingTicket;
import exceptions.SlotUnavailableException;

public class EntryGate {
    private ParkingSystem system;

    public EntryGate(ParkingSystem system) {
        this.system = system;
    }

    // MODIFIED signature
    public ParkingTicket processEntry(String username, String vehicleLicense, String vehicleType, String bookingId) throws SlotUnavailableException {
        return system.markEntry(username, vehicleLicense, vehicleType, bookingId);
    }
}