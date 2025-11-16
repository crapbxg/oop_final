package main;

import util.Constants;

public class Car extends Vehicle {
    public Car(String licensePlate, String ownerUsername) {
        super(licensePlate, ownerUsername, Constants.VEHICLE_CAR);
    }
}