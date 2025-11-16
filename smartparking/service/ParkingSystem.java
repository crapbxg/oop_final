package service; // Or your 'service' package

import main.*;
import exceptions.SlotUnavailableException;
import util.Constants;

public class ParkingSystem {
    private ParkingLot lot;
    private AuthService auth;
    // private BillingService billing; // REMOVED - You are correct, it's not used.

    private Booking[] bookings;
    private int bookingCount;
    private ParkingTicket[] activeTickets;
    private int ticketCount;

    // Constructor updated (billing parameter removed)
    public ParkingSystem(ParkingLot lot, AuthService auth) {
        this.lot = lot;
        this.auth = auth;
        // this.billing = billing; // REMOVED
        this.bookings = new Booking[1000];
        this.bookingCount = 0;
        this.activeTickets = new ParkingTicket[1000];
        this.ticketCount = 0;
    }

    // This method is now correct
    public Double markExitWithAmount(String ticketId, Double amount) throws Exception {
        for (int i = 0; i < ticketCount; i++) {
            ParkingTicket t = activeTickets[i];
            if (t != null && t.getTicketId().equals(ticketId) && t.isOpen()) {
                
                t.close(amount);
                
                String username = extractUsernameFromTicketId(ticketId);
                User u = auth.findUserByUsername(username);

                if (u instanceof Customer) {
                    Customer c = (Customer) u;
                    boolean paid = c.getAccount().processPayment(username, amount.doubleValue());
                    if (!paid) {
                        throw new Exception("Payment failed: insufficient funds");
                    }
                }
                
                ParkingSpot p = lot.findSpot(t.getSpotId());
                if (p != null) p.vacate();
                
                auth.removeCurrentUser(username);
                return amount;
            }
        }
        throw new Exception("Invalid ticket ID or ticket is already closed.");
    }

    // --- All other methods are unchanged ---

    public Booking bookSlot(Customer c, Vehicle v) throws SlotUnavailableException {
        BookingValidator validator = new BookingValidator();
        if (!validator.canBook(c, v)) {
            throw new SlotUnavailableException("Existing active booking for vehicle: " + v.getLicensePlate());
        }
        ParkingSpot p = lot.findFreeSpotForVehicle(v.getVehicleType());
        if (p == null) throw new SlotUnavailableException("No free spots available");
        
        String id = c.getUsername() + "_" + (bookingCount + 1);
        Booking b = new Booking(id, c.getUsername(), v.getLicensePlate(), p.getSpotId());
        if (bookingCount < bookings.length) bookings[bookingCount++] = b;
        return b;
    }

    public Booking bookSlot(Customer c, Vehicle v, String preferredSpotId) throws SlotUnavailableException {
        BookingValidator validator = new BookingValidator();
        if (!validator.canBook(c, v)) {
            throw new SlotUnavailableException("Existing active booking for vehicle: " + v.getLicensePlate());
        }
        ParkingSpot pref = lot.findSpot(preferredSpotId);
        if (pref != null && pref.isFree() && pref.getSpotType().equals(v.getVehicleType())) {
            String id = c.getUsername() + "_" + (bookingCount + 1);
            Booking b = new Booking(id, c.getUsername(), v.getLicensePlate(), pref.getSpotId());
            if (bookingCount < bookings.length) bookings[bookingCount++] = b;
            return b;
        }
        return bookSlot(c, v); // Fallback
    }

    public boolean cancelBooking(String bookingId, String username) {
        for (int i = 0; i < bookingCount; i++) {
            Booking b = bookings[i];
            if (b != null && b.getBookingId().equals(bookingId) && b.getUsername().equals(username) && b.isActive()) {
                b.cancel();
                return true;
            }
        }
        return false;
    }

    public ParkingTicket markEntry(String username, String vehicleLicense) {
        ParkingSpot chosen = null;
        for (int i = 0; i < bookingCount; i++) {
            Booking b = bookings[i];
            if (b != null && b.getUsername().equals(username) && b.getVehicleLicense().equals(vehicleLicense) && b.isActive()) {
                ParkingSpot p = lot.findSpot(b.getPreferredSpotId());
                if (p != null && (p.isFree() || p.getStatus().equals(Constants.SPOT_RESERVED))) {
                    chosen = p;
                    b.consume(); 
                    break;
                }
            }
        }

        if (chosen == null) {
            chosen = lot.findFreeSpotForVehicle(Constants.VEHICLE_CAR);
            if (chosen == null) {
                chosen = lot.findFreeSpotForVehicle(Constants.VEHICLE_MOTORCYCLE);
            }
        }

        if (chosen == null) return null;
        
        boolean ok = chosen.occupy(vehicleLicense);
        if (!ok) return null;
        
        String ticketId = username + "_T_" + (ticketCount + 1);
        ParkingTicket t = new ParkingTicket(ticketId, vehicleLicense, chosen.getSpotId());
        if (ticketCount < activeTickets.length) activeTickets[ticketCount++] = t;
        
        auth.addCurrentUser(username);
        return t;
    }

    public ParkingTicket findOpenTicketForUser(String username) {
        for (int i = 0; i < ticketCount; i++) {
            ParkingTicket t = activeTickets[i];
            if (t != null && t.isOpen() && t.getTicketId().startsWith(username + "_T_")) {
                return t;
            }
        }
        return null;
    }

    private String extractUsernameFromTicketId(String ticketId) {
        String[] parts = ticketId.split("_T_");
        if (parts.length >= 1) return parts[0];
        return ticketId;
    }

    public String getLiveStatus() {
        StringBuilder sb = new StringBuilder();
        Floor[] floors = lot.getFloors();
        for (int i = 0; i < floors.length; i++) {
            sb.append("Floor ").append(floors[i].getFloorNumber()).append("\n");
            ParkingSpot[] spots = floors[i].getSpots();
            for (int j = 0; j < spots.length; j++) {
                ParkingSpot p = spots[j];
                if (p == null) continue;
                sb.append("  ").append(p.getSpotId()).append(": ").append(p.getStatus());
                if(p.getStatus().equals(Constants.SPOT_OCCUPIED)) {
                     sb.append(" (").append(p.getCurrentVehicleLicense()).append(")");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Booking[] getActiveBookings() { return bookings; }
    public ParkingTicket[] getActiveTickets() { return activeTickets; }

    // Nested non-static class
    private class BookingValidator {
        public boolean canBook(Customer c, Vehicle v) {
            for (int i = 0; i < bookingCount; i++) {
                Booking b = bookings[i];
                if (b == null) continue;
                if (b.getVehicleLicense().equals(v.getLicensePlate()) && b.isActive()) {
                    return false; 
                }
            }
            return true;
        }
    }
}