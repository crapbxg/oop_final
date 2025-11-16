package main;

import util.Reportable;
import util.Loggable;
import util.Constants;

public class Admin extends User implements Reportable, Loggable {
    private String[] auditLogs = new String[500];
    private int logCount = 0;

    public Admin(String username, String hashedPassword, String name, String email) {
        super(username, hashedPassword, name, email, Constants.ROLE_ADMIN);
    }

    public Admin(String username, String hashedPassword) {
        super(username, hashedPassword, "Admin", "admin@local", Constants.ROLE_ADMIN);
       
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Admin Dashboard for " + name + " ---");
    }

    @Override
    public String generateReport() {
        return "Report (stub)";
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
    }
}