package main;

public class ParkingTicket {
    private String ticketId;
    private String vehicleLicense;
    private String vehicleType;
    private String spotId;
    private long entryTimeMillis;
    // private long exitTimeMillis; // <-- REMOVED
    private boolean open;
    private Double amountDue;
    private double surcharge; 

    public ParkingTicket(String ticketId, String vehicleLicense, String vehicleType, String spotId, long entryTimeMillis) {
        this.ticketId = ticketId;
        this.vehicleLicense = vehicleLicense;
        this.vehicleType = vehicleType;
        this.spotId = spotId;
        this.entryTimeMillis = entryTimeMillis;
        this.open = true;
        this.amountDue = 0.0;
        this.surcharge = 0.0; 
    }

    public String getTicketId() { return ticketId; }
    public String getVehicleLicense() { return vehicleLicense; }
    public String getVehicleType() { return vehicleType; }
    public String getSpotId() { return spotId; }
    public long getEntryTimeMillis() { return entryTimeMillis; }
    public boolean isOpen() { return open; }
    public Double getAmountDue() { return amountDue; }

    public void close(long exitTime, Double amount) {
        // this.exitTimeMillis = exitTime; // <-- REMOVED
        this.open = false;
        this.amountDue = amount;
    }
    
    public void setSurcharge(double surcharge) {
        this.surcharge = surcharge;
    }
    
    public double getSurcharge() {
        return surcharge;
    }
}