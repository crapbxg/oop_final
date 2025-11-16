package service;

import main.*;
//import exceptions.InsufficientFundsException;
import exceptions.SlotUnavailableException;
import util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; 

public class ParkingSystem {
    private ParkingLot lot;
    private AuthService auth;
    private BillingService billing;

    // Use dynamic lists instead of fixed arrays
    private List<Booking> bookings;
    private List<ParkingTicket> allTickets; // Renamed from activeTickets
    private int ticketCounter = 1000; // Simple ticket ID generator

    public ParkingSystem(ParkingLot lot, AuthService auth, BillingService billing) {
        this.lot = lot;
        this.auth = auth;
        this.billing = billing;
        this.bookings = new ArrayList<>();
        this.allTickets = new ArrayList<>(); // Renamed from activeTickets
    }

    // --- BOOKING LOGIC ---

    public Booking bookSlot(Customer c, Vehicle v, String preferredSpotId) throws SlotUnavailableException {
        ParkingSpot p = null;

        if (preferredSpotId != null && !preferredSpotId.isEmpty()) {
            // --- Preferred Spot Booking ---
            p = lot.findSpot(preferredSpotId);
            if (p == null) {
                throw new SlotUnavailableException("Spot ID " + preferredSpotId + " does not exist.");
            }
            if (!p.isFree()) {
                throw new SlotUnavailableException("Spot " + preferredSpotId + " is not free.");
            }
            if (!p.getSpotType().equalsIgnoreCase(v.getVehicleType())) {
                throw new SlotUnavailableException("Spot " + preferredSpotId + " is for " + p.getSpotType() + ", not " + v.getVehicleType());
            }
        } else {
            // --- Auto-assign Booking ---
            p = lot.findFreeSpotForVehicle(v.getVehicleType());
            if (p == null) {
                throw new SlotUnavailableException("No free spots available for " + v.getVehicleType());
            }
        }
        
        String id = c.getUsername() + "_" + (bookings.size() + 1);
        
        // Create booking with no duration
        Booking b = new Booking(id, c.getUsername(), v.getLicensePlate(), v.getVehicleType(), p.getSpotId());
        bookings.add(b);
        p.reserve(c.getUsername()); // Mark spot as reserved
        return b;
    }

    // --- ENTRY/EXIT LOGIC ---

    public ParkingTicket markEntry(String username, String vehicleLicense, String vehicleType, String bookingId) throws SlotUnavailableException {
        ParkingSpot chosen = null;
        String licenseForTicket = vehicleLicense;
        String vehicleTypeForTicket = vehicleType;
        double surcharge = 0.0; // Default no surcharge

        // 1. Check for an active, valid booking
        if (bookingId != null && !bookingId.isEmpty()) {
            Booking b = findBookingById(bookingId);
            
            if (b == null || !b.getUsername().equals(username) || !b.isActive()) {
                throw new SlotUnavailableException("Invalid or inactive booking ID.");
            }
            
            ParkingSpot p = lot.findSpot(b.getPreferredSpotId());
            // Check if spot is RESERVED (by this user) or still FREE (if reservation timed out, but for us, it's fine)
            if (p != null && (p.getStatus().equals(Constants.SPOT_RESERVED) || p.getStatus().equals(Constants.SPOT_FREE))) {
                chosen = p;
                b.consume(); // Mark booking as used
                licenseForTicket = b.getVehicleLicense();
                vehicleTypeForTicket = b.getVehicleType();
                // surcharge remains 0.0 for bookings
            } else {
                throw new SlotUnavailableException("Reserved spot " + b.getPreferredSpotId() + " is no longer available.");
            }
        } else {
            // 2. If no valid booking, find any free spot for this type (On-Spot)
            chosen = lot.findFreeSpotForVehicle(vehicleType);
            if (chosen == null) {
                throw new SlotUnavailableException("No free spots available for " + vehicleType);
            }
            surcharge = 50.0; // Apply on-spot surcharge
        }
        
        // 3. Occupy spot and create ticket
        chosen.occupy(licenseForTicket);
        String ticketId = "TKT" + (++ticketCounter);
        long entryTime = System.currentTimeMillis();
        
        ParkingTicket t = new ParkingTicket(ticketId, licenseForTicket, vehicleTypeForTicket, chosen.getSpotId(), entryTime);
        t.setSurcharge(surcharge); // Set surcharge (0.0 or 50.0)
        allTickets.add(t);
        auth.addCurrentUser(username);
        return t;
    }

    public Double markExit(String ticketId, Customer customer) throws Exception {
        ParkingTicket ticket = findOpenTicket(ticketId);
        if (ticket == null) {
            throw new Exception("Invalid or already-closed ticket ID.");
        }

        long exitTime = System.currentTimeMillis();
        
        // 1. Calculate fee based on duration
        Double amount = billing.calculateFee(ticket.getEntryTimeMillis(), exitTime, ticket.getVehicleType());
        
        // 2. Add surcharge (if any)
        amount += ticket.getSurcharge();

        // 3. Process payment
        boolean paid = customer.getAccount().processPayment(customer.getUsername(), amount);
        if (!paid) {
            // This is where InsufficientFundsException is used!
            throw new exceptions.InsufficientFundsException("Payment failed. Amount due: " + amount + ", Balance: " + customer.getAccount().getBalance());
        }

        // 4. Close ticket, vacate spot, log user out of record.txt
        ticket.close(exitTime, amount);
        ParkingSpot p = lot.findSpot(ticket.getSpotId());
        if (p != null) p.vacate();
        
        auth.removeCurrentUser(customer.getUsername());
        return amount;
    }

    // --- HELPER & REPORTING METHODS ---

    public ParkingTicket findOpenTicket(String ticketId) {
        for (ParkingTicket t : allTickets) { // Search allTickets
            if (t.getTicketId().equals(ticketId) && t.isOpen()) {
                return t;
            }
        }
        return null;
    }
    
    private Booking findBookingById(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                return b;
            }
        }
        return null;
    }

    public ParkingTicket findOpenTicketForUser(String username) {
        // This logic is complex and would require linking tickets to users directly,
        // which is not currently implemented.
        return null; 
    }
    
    public String generateSystemReport() {
        int occupied = 0;
        int free = 0;
        for (Floor f : lot.getFloors()) {
            for (ParkingSpot p : f.getSpots()) {
                if (p != null) {
                    if (p.isFree()) free++;
                    else occupied++;
                }
            }
        }
        // Count only OPEN tickets as "active"
        long openTickets = allTickets.stream().filter(ParkingTicket::isOpen).count();

        return String.format("--- System Report ---\nOccupied Spots: %d\nFree Spots: %d\nActive Tickets: %d\n",
            occupied, free, openTickets);
    }

    public String getParkingHistory(Customer c) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Parking History for ").append(c.getUsername()).append(" ---\n");
        
        // Get customer's registered vehicle plates
        List<String> customerPlates = Arrays.asList(c.getVehiclePlates());
        
        if (customerPlates.isEmpty()) {
            return sb.append("No vehicles registered to this account.\n").toString();
        }
        
        int found = 0;
        for (ParkingTicket t : allTickets) {
            // Find tickets (open or closed) matching any of the customer's vehicles
            if (customerPlates.contains(t.getVehicleLicense())) {
                 sb.append(String.format("Ticket: %s, Spot: %s\n",
                    t.getTicketId(), t.getSpotId()));
                 found++;
            }
        }
        
        if (found == 0) {
            sb.append("No parking history found.\n");
        }
        return sb.toString();
    }

    public String getLiveStatus() {
        // ... (This method is fine, but we need to add spot type to the display) ...
        StringBuilder sb = new StringBuilder();
        for (Floor f : lot.getFloors()) {
            sb.append("--- Floor ").append(f.getFloorNumber()).append(" ---\n");
            for (ParkingSpot p : f.getSpots()) {
                if (p == null) continue;
                sb.append(String.format("  Spot %s (%s): %s", p.getSpotId(), p.getSpotType(), p.getStatus()));
                if (p.getStatus().equals(Constants.SPOT_OCCUPIED)) {
                    sb.append(" (").append(p.getCurrentVehicleLicense()).append(")");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}