package main;

// Simplified: No timestamps
public class ParkingTicket {
    private String ticketId;
    private String vehicleLicense;
    private String spotId;
    private boolean open;
    private Double amountDue;

    public ParkingTicket(String ticketId, String vehicleLicense, String spotId) {
        this.ticketId = ticketId;
        this.vehicleLicense = vehicleLicense;
        this.spotId = spotId;
        this.open = true;
        this.amountDue = null;
    }

    public String getTicketId() { return ticketId; }
    public String getVehicleLicense() { return vehicleLicense; }
    public String getSpotId() { return spotId; }
    public boolean isOpen() { return open; }
    public Double getAmountDue() { return amountDue; }

    public void close(Double amount) {
        this.open = false;
        this.amountDue = amount;
    }
}