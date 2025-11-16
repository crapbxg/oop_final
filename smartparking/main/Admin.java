package main;

import util.Reportable;
import util.Loggable;
import util.Constants;
import service.ParkingSystem; // Import ParkingSystem

public class Admin extends User implements Reportable, Loggable {
    private String[] auditLogs = new String[500];
    private int logCount = 0;
    private ParkingSystem system; // Field to hold the system

    public Admin(String username, String hashedPassword, String name, String email) {
        super(username, hashedPassword, name, email, Constants.ROLE_ADMIN);
    }
    
    // Method to link the admin to the main system
    public void setSystem(ParkingSystem system) {
        this.system = system;
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Admin Dashboard for " + name + " ---");
    }

    @Override
    public String generateReport() {
        if (system == null) {
            return "Report (stub): System not linked.";
        }
        // Generate a real report
        return system.generateSystemReport();
    }

    @Override
    public void logActivity(String message, Object... args) {
        String m = String.format(message, args);
        if (logCount < auditLogs.length) {
            auditLogs[logCount++] = m;
        }
        System.out.println("[ADMIN] " + m);
    }

    public void addSpots(int floorNumber, String... spotIds) {
        logActivity("Add %d spots to floor %d", spotIds.length, floorNumber);
        // In a real system, this would call system.getLot().addSpotToFloor(...)
    }
}