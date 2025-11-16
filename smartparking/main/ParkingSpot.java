package main;

import util.Constants;

public class ParkingSpot {
    private String spotId;
    private String spotType;
    private String status;
    private String currentVehicleLicense;


    public ParkingSpot(String spotId) {
        this.spotId = spotId;
        this.spotType = Constants.VEHICLE_CAR;
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public ParkingSpot(String spotId, String spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public boolean occupy(String vehicleLicense) {
        if (!status.equals(Constants.SPOT_FREE) && !status.equals(Constants.SPOT_RESERVED)) {
            return false;
        }
        this.currentVehicleLicense = vehicleLicense;
        this.status = Constants.SPOT_OCCUPIED;
        return true;
    }

    public boolean vacate() {
        if (!status.equals(Constants.SPOT_OCCUPIED)) {
            return false;
        }
        this.currentVehicleLicense = "";
        this.status = Constants.SPOT_FREE;
        return true;
    }

    public boolean reserve(long untilMillis, String username) {
        if (!status.equals(Constants.SPOT_FREE)) {
            return false;
        }
        this.status = Constants.SPOT_RESERVED;
        return true;
    }

    public boolean isFree() {
        return status.equals(Constants.SPOT_FREE);
    }

    public String getSpotId() {
        return spotId;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrentVehicleLicense() {
        return currentVehicleLicense;
    }

    public void forceFree() {
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public String getSpotType() {
        return spotType;
    }
}