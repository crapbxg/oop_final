package main; // Or 'main' if you are using that

import util.Constants;

/**
 * Attendant class. In this design, it's a simple User role.
 * The CLI (handleAttendant) provides its specific menu and actions.
 */
public class Attendant extends User {

    public Attendant(String username, String hashedPassword, String name, String email) {
        super(username, hashedPassword, name, email, Constants.ROLE_ATTENDANT);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Attendant Dashboard: " + name + " ---");
        System.out.println("Ready to process vehicle entry and exit.");
    }
}