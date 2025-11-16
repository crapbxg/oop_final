package main;

import util.Constants;

public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate, String ownerUsername) {
        super(licensePlate, ownerUsername, Constants.VEHICLE_MOTORCYCLE);
    }
}