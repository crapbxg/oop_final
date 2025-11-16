package main;

import util.Constants;

public class ParkingSpot {
    private String spotId;
    private String spotType;
    private String status;
    private String currentVehicleLicense;


    public ParkingSpot(String spotId) {
        // ... (constructor unchanged) ...
        this.spotId = spotId;
        this.spotType = Constants.VEHICLE_CAR;
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public ParkingSpot(String spotId, String spotType) {
        // ... (constructor unchanged) ...
        this.spotId = spotId;
        this.spotType = spotType;
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public boolean occupy(String vehicleLicense) {
        // ... (occupy method unchanged) ...
        if (!status.equals(Constants.SPOT_FREE) && !status.equals(Constants.SPOT_RESERVED)) {
            return false;
        }
        this.currentVehicleLicense = vehicleLicense;
        this.status = Constants.SPOT_OCCUPIED;
        return true;
    }

    public boolean vacate() {
        // ... (vacate method unchanged) ...
        if (!status.equals(Constants.SPOT_OCCUPIED)) {
            return false;
        }
        this.currentVehicleLicense = "";
        this.status = Constants.SPOT_FREE;
        return true;
    }

    public boolean reserve(String username) { // MODIFIED (removed untilMillis)
        if (!status.equals(Constants.SPOT_FREE)) {
            return false;
        }
        this.status = Constants.SPOT_RESERVED;
        return true;
    }

    public boolean isFree() {
        // ... (isFree method unchanged) ...
        return status.equals(Constants.SPOT_FREE);
    }

    public String getSpotId() {
        // ... (getSpotId method unchanged) ...
        return spotId;
    }

    public String getStatus() {
        // ... (getStatus method unchanged) ...
        return status;
    }

    public String getCurrentVehicleLicense() {
        // ... (getCurrentVehicleLicense method unchanged) ...
        return currentVehicleLicense;
    }

    public void forceFree() {
        // ... (forceFree method unchanged) ...
        this.status = Constants.SPOT_FREE;
        this.currentVehicleLicense = "";
    }

    public String getSpotType() {
        // ... (getSpotType method unchanged) ...
        return spotType;
    }
}