package main;

public class Booking {
    private String bookingId;
    private String username;
    private String vehicleLicense;
    private String vehicleType; // ADDED
    private String preferredSpotId;
    private String status; // ACTIVE, CANCELLED, CONSUMED
    // REMOVED startTimeMillis
    // REMOVED durationMillis

    public Booking(String bookingId, String username, String vehicleLicense, String vehicleType, String preferredSpotId) {
        this.bookingId = bookingId;
        this.username = username;
        this.vehicleLicense = vehicleLicense;
        this.vehicleType = vehicleType; // ADDED
        this.preferredSpotId = preferredSpotId;
        // REMOVED startTime
        // REMOVED duration
        this.status = "ACTIVE";
    }

    public String getBookingId() { return bookingId; }
    public String getUsername() { return username; }
    public String getVehicleLicense() { return vehicleLicense; }
    public String getVehicleType() { return vehicleType; } // ADDED
    public String getPreferredSpotId() { return preferredSpotId; }
    public String getStatus() { return status; }
    // REMOVED getStartTimeMillis()
    // REMOVED getEndTimeMillis()

    public void cancel() { this.status = "CANCELLED"; }
    public void consume() { this.status = "CONSUMED"; }
    
    public boolean isActive() {
        // Check if it's "ACTIVE"
        return "ACTIVE".equals(this.status);
    }
}