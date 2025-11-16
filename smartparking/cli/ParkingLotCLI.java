package cli;

import main.*;
import service.*;
import util.Constants;
import util.AuthUtil;
import exceptions.UserAlreadyExistsException;
// import exceptions.InvalidHoursException; // This was already correctly removed
import exceptions.SlotUnavailableException;

import java.util.Arrays;
import java.util.Scanner;
import java.io.File;

public class ParkingLotCLI {

    private ParkingSystem system;
    private AuthService auth;
    private Scanner scanner;
    private boolean running;
    // private BillingService billing; // <-- REMOVED

    public ParkingLotCLI(ParkingSystem system, AuthService auth) { // <-- REMOVED from params
        this.system = system;
        this.auth = auth;
        this.scanner = new Scanner(System.in);
        this.running = true;
        // this.billing = billing; // <-- REMOVED
    }

    public static void main(String[] args) {
        try {
            new File("record.txt").createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating record.txt: " + e.getMessage());
        }

        AuthService auth = new AuthService("record.txt");
        ParkingLot lot = new ParkingLot(2, 8); // 2 floors, 8 spots each
        
        // Floor 1: 5 Car Spots, 3 Bike Spots
        for (int i = 1; i <= 5; i++) lot.addSpotToFloor(1, new ParkingSpot("A" + i, Constants.VEHICLE_CAR));
        for (int i = 6; i <= 8; i++) lot.addSpotToFloor(1, new ParkingSpot("A" + i, Constants.VEHICLE_MOTORCYCLE));
        // Floor 2: 5 Car Spots, 3 Bike Spots
        for (int i = 1; i <= 5; i++) lot.addSpotToFloor(2, new ParkingSpot("B" + i, Constants.VEHICLE_CAR));
        for (int i = 6; i <= 8; i++) lot.addSpotToFloor(2, new ParkingSpot("B" + i, Constants.VEHICLE_MOTORCYCLE));
        System.out.println("Parking lot initialized with Car and Bike spots.");

        // Billing: Car=20/hr, Bike=10/hr, Min=10
        BillingService billing = new BillingService(20.0, 10.0, 10.0);
        ParkingSystem system = new ParkingSystem(lot, auth, billing);

        // Load Demo Users
        try {
            Admin admin = new Admin("admin", AuthUtil.hash("admin123"), "Admin User", "admin@park.com");
            admin.setSystem(system); // Link admin to system for reports
            auth.registerUser(admin);
            
            auth.registerUser(new Attendant("att1", AuthUtil.hash("att123"), "Attendant One", "att@park.com"));
            Customer john = new Customer("john", AuthUtil.hash("john123"), "John Doe", "john@email.com", 150.0);
            john.addVehicle("JH-01-9999"); // Add a vehicle for John
            auth.registerUser(john);
            System.out.println("Demo users loaded (admin/admin123, att1/att123, john/john123)");
        } catch (UserAlreadyExistsException e) {
            System.out.println("Demo users already exist.");
        }
        
        ParkingLotCLI cli = new ParkingLotCLI(system, auth); // <-- REMOVED from args
        cli.run();
    }

    // ... (rest of the file is unchanged) ...
    public void run() {
        System.out.println("Welcome to SmartParking CLI (Timer-Based Billing)");
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": handleRegister(); break;
                    case "2": handleLogin(); break;
                    case "3": showLiveStatus(); break;
                    case "4": showCurrentUsersInside(); break;
                    case "0": running = false; System.out.println("Exiting..."); break;
                    default: System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                printError("Error: %s", e.getMessage());
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Register (Customer)");
        System.out.println("2. Login");
        System.out.println("3. Show Live Slot Status");
        System.out.println("4. Show users currently inside (record.txt)");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private void handleRegister() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String pass = scanner.nextLine().trim();
            System.out.print("Enter full name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            String hashed = AuthUtil.hash(pass);
            Customer c = new Customer(username, hashed, name, email, 100.0);
            
            // Ask to add one vehicle on registration
            System.out.print("Enter your primary vehicle license plate (e.g., MH-01-1234): ");
            String plate = scanner.nextLine().trim();
            c.addVehicle(plate);

            auth.registerUser(c);
            System.out.println("Registered successfully. Demo balance: ₹100");
        } catch (UserAlreadyExistsException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();
        User u = auth.login(username, pass);
        if (u == null) {
            System.out.println("Login failed.");
            return;
        }
        System.out.println("Welcome " + u.getName() + " (" + u.getRole() + ")");
        if (u instanceof Customer) handleCustomer((Customer) u);
        else if (u instanceof Admin) handleAdmin((Admin) u);
        else if (u instanceof Attendant) handleAttendant((Attendant) u);
    }

    // --- Customer Menu (Req #2, #4, #5) ---
    private void handleCustomer(Customer c) {
        boolean back = false;
        while (!back) {
            
            // Check user's "inside" status at the start of every loop
            boolean isInside = checkUserInside(c.getUsername());

            if (isInside) {
                // --- USER IS INSIDE THE LOT ---
                System.out.println("\n--- Customer Menu (" + c.getUsername() + ") [INSIDE] ---");
                System.out.println("1. Exit Parking (Pay)");
                System.out.println("2. View Dashboard");
                System.out.println("9. Logout");
                System.out.print("Choice: ");
                String ch = scanner.nextLine().trim();
                try {
                    switch (ch) {
                        case "1": handleExit(c); break;
                        case "2": c.showDashboard(); break;
                        case "9": back = true; break;
                        default: System.out.println("Invalid");
                    }
                } catch (Exception e) {
                    printError("Error: %s", e.getMessage());
                }

            } else {
                // --- USER IS OUTSIDE THE LOT ---
                System.out.println("\n--- Customer Menu (" + c.getUsername() + ") [OUTSIDE] ---");
                System.out.println("1. View Dashboard");
                System.out.println("2. Book Slot (Reservation)");
                System.out.println("3. Enter Parking");
                System.out.println("4. View Available Slots");
                System.out.println("5. View My Parking History");
                System.out.println("6. Deposit Money");
                System.out.println("9. Logout");
                System.out.print("Choice: ");
                String ch = scanner.nextLine().trim();
                try {
                    switch (ch) {
                        case "1": c.showDashboard(); break;
                        case "2": handleBookSlot(c); break;
                        case "3": handleEntry(c); break;
                        case "4": showLiveStatus(); break;
                        case "5": System.out.println(system.getParkingHistory(c)); break;
                        case "6": handleDeposit(c); break;
                        case "9": back = true; break;
                        default: System.out.println("Invalid");
                    }
                } catch (Exception e) {
                    printError("Error: %s", e.getMessage());
                }
            }
        }
    }

    // --- Admin Menu (Req #5) ---
    private void handleAdmin(Admin a) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Dashboard");
            System.out.println("2. View Live Status");
            System.out.println("3. Generate System Report");
            System.out.println("4. Manage Spots (Placeholder)");
            System.out.println("9. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": a.showDashboard(); break;
                case "2": showLiveStatus(); break;
                case "3": System.out.println(a.generateReport()); break;
                case "4": a.addSpots(1, "D1", "D2"); break;
                case "9": back = true; break;
                default: System.out.println("Invalid");
            }
        }
    }

    // --- Attendant Menu (Req #5) ---
    private void handleAttendant(Attendant at) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Attendant Menu ---");
            System.out.println("1. Mark Entry");
            System.out.println("2. Mark Exit");
            System.out.println("3. Assist Customer (Placeholder)");
            System.out.println("9. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();
            try {
                switch (ch) {
                    case "1":
                        System.out.print("Enter Customer's Username: ");
                        String user = scanner.nextLine().trim();
                        Customer c = (Customer) auth.findUserByUsername(user);
                        if(c == null) {
                            System.out.println("Customer not found.");
                            break;
                        }
                        handleEntry(c); // Reuse the customer entry flow
                        break;
                    case "2":
                        System.out.print("Enter Customer's Username (who is paying): ");
                        String userExit = scanner.nextLine().trim();
                        Customer cExit = (Customer) auth.findUserByUsername(userExit);
                        if(cExit == null) {
                            System.out.println("Customer not found.");
                            break;
                        }
                        handleExit(cExit); // Reuse the customer exit flow
                        break;
                    case "3":
                        System.out.println("Assisted customer.");
                        break;
                    case "9": back = true; break;
                    default: System.out.println("Invalid");
                }
            } catch (Exception e) {
                printError("Error: " + e.getMessage());
            }
        }
    }
    
    // --- Helper Methods ---

    private Vehicle getVehicleFromUser(Customer c) {
        // (Req #3)
        System.out.println("Select Vehicle Type:");
        System.out.println("1. Car (" + Constants.VEHICLE_CAR + ")");
        System.out.println("2. Bike (" + Constants.VEHICLE_MOTORCYCLE + ")");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        String type = typeChoice.equals("2") ? Constants.VEHICLE_MOTORCYCLE : Constants.VEHICLE_CAR;
        
        System.out.print("Enter License Plate: ");
        String plate = scanner.nextLine().trim();
        
        // Add vehicle to customer's profile if not already present
        boolean exists = false;
        for (String p : c.getVehiclePlates()) {
            if (p != null && p.equals(plate)) { 
                exists = true;
                break;
            }
        }
        if (!exists) c.addVehicle(plate);
        
        if (type.equals(Constants.VEHICLE_CAR)) return new Car(plate, c.getUsername());
        else return new Motorcycle(plate, c.getUsername());
    }

    private void handleBookSlot(Customer c) throws SlotUnavailableException {
        System.out.println("--- Book a Slot ---");
        Vehicle v = getVehicleFromUser(c);
        
        System.out.println("1. Auto-assign best spot");
        System.out.println("2. Choose a specific spot");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        String preferredSpotId = null;

        if (choice.equals("2")) {
            System.out.print("Enter preferred Spot ID (e.g., A1): ");
            preferredSpotId = scanner.nextLine().trim().toUpperCase();
        }

        Booking b = system.bookSlot(c, v, preferredSpotId);
        System.out.println("SUCCESS! Slot " + b.getPreferredSpotId() + " reserved.");
        System.out.println("Booking ID: " + b.getBookingId());
    }
    
    private void handleEntry(Customer c) throws Exception {
        System.out.println("--- Parking Entry ---");
        System.out.println("1. On-Spot Entry (Drive-in)");
        System.out.println("2. Reserved Entry (Use Booking ID)");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        
        ParkingTicket t = null;
        EntryGate gate = new EntryGate(system);

        if (choice.equals("1")) {
            // --- ON-SPOT ENTRY ---
            System.out.println("On-Spot entry will incur a ₹50.0 surcharge on exit.");
            Vehicle v = getVehicleFromUser(c);
            t = gate.processEntry(c.getUsername(), v.getLicensePlate(), v.getVehicleType(), null);
            System.out.println("Welcome " + c.getName() + "!");

        } else if (choice.equals("2")) {
            // --- RESERVED ENTRY ---
            System.out.print("Enter your Booking ID: ");
            String bookingId = scanner.nextLine().trim();
            t = gate.processEntry(c.getUsername(), null, null, bookingId);
            System.out.println("Welcome " + c.getName() + "!");
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        
        System.out.println("Entry successful. Please remember your ticket ID.");
        System.out.println("Ticket ID: " + t.getTicketId());
        System.out.println("Spot: " + t.getSpotId() + " (" + t.getVehicleType() + ")");
    }

    private void handleExit(Customer c) throws Exception {
        // (Req #4, #6)
        System.out.println("--- Exit & Pay ---");
        System.out.print("Enter your Ticket ID: ");
        String tid = scanner.nextLine().trim();
        
        Double amt = new ExitGate(system).processExit(tid, c);
        
        System.out.println("Exit successful.");
        System.out.println("Total charge: ₹" + amt);
        System.out.println("New balance: ₹" + c.getAccount().getBalance());
    }
    
    private void handleDeposit(Customer c) {
        System.out.print("Enter amount to deposit: ");
        try {
            double amt = Double.parseDouble(scanner.nextLine().trim());
            c.getAccount().deposit(amt);
            System.out.println("Deposit successful. New balance: ₹" + c.getAccount().getBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
    }
    
    // --- System-Wide Methods ---

    private void showLiveStatus() {
        System.out.println(system.getLiveStatus());
    }

    private void showCurrentUsersInside() {
        String[] users = auth.readCurrentUsers();
        System.out.println("Users currently inside (record.txt):");
        if (users.length == 0) System.out.println("(none)");
        for (String u : users) System.out.println("- " + u);
    }

    private boolean checkUserInside(String username) {
        String[] users = auth.readCurrentUsers();
        return Arrays.asList(users).contains(username);
    }

    private void printError(String message, Object... args) {
        String m = String.format(message, args);
        System.err.println(m);
    }
}