package main;

public abstract class Vehicle {
    protected String licensePlate;
    protected String ownerUsername;
    protected String vehicleType;

    public Vehicle(String licensePlate, String ownerUsername, String vehicleType) {
        this.licensePlate = licensePlate;
        this.ownerUsername = ownerUsername;
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() { 
        return licensePlate; 
    }
    public String getOwnerUsername() { 
        return ownerUsername; 
    }
    public String getVehicleType() { 
        return vehicleType; 
    }
}