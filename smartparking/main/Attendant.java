package main;

import util.Constants;

public class Attendant extends User {
   

    public Attendant(String username, String hashedPassword, String name, String email) {
        super(username, hashedPassword, name, email, Constants.ROLE_ATTENDANT);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Attendant Dashboard: " + name + " ---");
    }
}