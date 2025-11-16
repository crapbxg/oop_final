package main;

// Simplified: No timestamps, just status
public class Booking {
    private String bookingId;
    private String username;
    private String vehicleLicense;
    private String preferredSpotId;
    private String status; // ACTIVE, CANCELLED, CONSUMED

    public Booking(String bookingId, String username, String vehicleLicense, String preferredSpotId) {
        this.bookingId = bookingId;
        this.username = username;
        this.vehicleLicense = vehicleLicense;
        this.preferredSpotId = preferredSpotId;
        this.status = "ACTIVE";
    }

    public String getBookingId() { return bookingId; }
    public String getUsername() { return username; }
    public String getVehicleLicense() { return vehicleLicense; }
    public String getPreferredSpotId() { return preferredSpotId; }
    public String getStatus() { return status; }

    public void cancel() { this.status = "CANCELLED"; }
    public void consume() { this.status = "CONSUMED"; }
    public boolean isActive() { return "ACTIVE".equals(this.status); }
}