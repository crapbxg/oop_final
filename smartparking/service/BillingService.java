package service;

/**
 * BillingService - charge by integer hours.
 */
public class BillingService {
    private Double hourlyRate;
    private Double minimumCharge;

    public BillingService(Double hourlyRate) {
        this(hourlyRate, 0.0);
    }

    public BillingService(Double hourlyRate, Double minimumCharge) {
        this.hourlyRate = hourlyRate;
        this.minimumCharge = minimumCharge;
    }

    public Double calculateFeeByHours(int hours) {
        if (hours <= 0) return Double.valueOf(0.0);
        double raw = hours * hourlyRate.doubleValue();
        if (raw < minimumCharge.doubleValue()) raw = minimumCharge.doubleValue();
        return Double.valueOf(roundTo2Decimals(raw));
    }

    private double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
