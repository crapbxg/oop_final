package service;

import util.Constants;

/**
 * BillingService - charges based on actual duration and vehicle type.
 */
public class BillingService {
    private Double carHourlyRate;
    private Double bikeHourlyRate;
    private Double minimumCharge;

    public BillingService(Double carHourlyRate, Double bikeHourlyRate, Double minimumCharge) {
        this.carHourlyRate = carHourlyRate;
        this.bikeHourlyRate = bikeHourlyRate;
        this.minimumCharge = minimumCharge;
    }

    /**
     * Calculates fee based on duration and vehicle type.
     * Charges for a minimum of 1 hour.
     */
    public Double calculateFee(long entryMillis, long exitMillis, String vehicleType) {
        long durationMillis = exitMillis - entryMillis;
        if (durationMillis < 0) return 0.0;

        // Convert milliseconds to hours, and use Math.ceil to round UP.
        // e.g., 10 minutes (0.16 hours) -> 1 hour
        // e.g., 61 minutes (1.01 hours) -> 2 hours
        double hours = Math.ceil(durationMillis / (1000.0 * 60.0 * 60.0));
        
        // Enforce minimum 1 hour charge
        if (hours < 1.0) {
            hours = 1.0;
        }

        double rate = vehicleType.equals(Constants.VEHICLE_MOTORCYCLE) ? bikeHourlyRate : carHourlyRate;
        double rawFee = hours * rate;

        // Return the calculated fee, or the minimum charge, whichever is greater
        return Math.max(rawFee, minimumCharge);
    }

}