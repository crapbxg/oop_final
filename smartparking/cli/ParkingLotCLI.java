package cli;

import main.*;
import service.*;
import util.Constants;
import util.AuthUtil;
import exceptions.UserAlreadyExistsException;
import exceptions.InvalidHoursException;

import java.util.Scanner;
import java.io.File;

/**
 * Final CLI:
 * - Creates the single 'record.txt' file.
 * - Loads demo users into the in-memory AuthService.
 * - Handles all user input and menu navigation.
 * - Catches all custom exceptions.
 * - BookingExpiryService has been REMOVED.
 */
public class ParkingLotCLI {

    private ParkingSystem system;
    private AuthService auth;
    // private BookingExpiryService expiryService; // REMOVED
    private Scanner scanner;
    private boolean running;
    private BillingService billing; // KEPT - This is used by the CLI

    // Constructor updated (expiryService parameter removed)
    public ParkingLotCLI(ParkingSystem system, AuthService auth, BillingService billing) {
        this.system = system;
        this.auth = auth;
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.billing = billing; // Store the billing service
    }

    public static void main(String[] args) {
        try {
            new File("record.txt").createNewFile();
        } catch (Exception e) {
            System.out.println("Error creating record.txt: " + e.getMessage());
        }

        AuthService auth = new AuthService("record.txt");

        try {
            System.out.println("Loading demo users...");
            auth.registerUser(new Admin("admin", AuthUtil.hash("admin123"), "Admin User", "admin@park.com"));
            auth.registerUser(new Attendant("att1", AuthUtil.hash("att123"), "Attendant One", "att@park.com"));
            auth.registerUser(new Customer("john", AuthUtil.hash("john123"), "John Doe", "john@email.com", 150.0));
            System.out.println("Demo users loaded (admin/admin123, att1/att123, john/john123)");
        } catch (UserAlreadyExistsException e) {
            System.out.println("Demo users already exist.");
        }
        
        ParkingLot lot = new ParkingLot(2, 8); // 2 floors, 8 spots
        for (int f = 1; f <= 2; f++) {
            for (int i = 1; i <= 8; i++) {
                String id = (char) ('A' + (f - 1)) + String.format("%02d", i);
                lot.addSpotToFloor(f, new ParkingSpot(id, Constants.VEHICLE_CAR));
            }
        }

        BillingService billing = new BillingService(Double.valueOf(20.0));
        
        // Use the simple constructor (no billing)
        ParkingSystem system = new ParkingSystem(lot, auth); 
        
        // CLI creation updated
        ParkingLotCLI cli = new ParkingLotCLI(system, auth, billing);
        cli.run();
    }

    public void run() {
        System.out.println("Welcome to SmartParking CLI (single-file mode: record.txt)");
        while (running) {
            // expiryService.checkOnce(); // REMOVED
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
            
            auth.registerUser(c);
            System.out.println("Registered successfully (in-memory). Demo balance: ₹100");
        
        } catch (UserAlreadyExistsException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Registration failed: An unknown error occurred.");
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();
        User u = auth.login(username, pass);
        if (u == null) {
            System.out.println("Login failed (wrong credentials or not registered).");
            return;
        }
        System.out.println("Welcome " + u.getName() + " (" + u.getRole() + ")");
        if (u instanceof Customer) handleCustomer((Customer) u);
        else if (u instanceof Admin) handleAdmin((Admin) u);
        else if (u instanceof Attendant) handleAttendant((Attendant) u);
        else System.out.println("Unknown role");
    }

    private void handleCustomer(Customer c) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Dashboard");
            System.out.println("2. Book Slot (auto)");
            System.out.println("3. Book Slot (preferred)");
            System.out.println("4. Enter parking (simulate)");
            System.out.println("5. Exit parking (simulate)");
            System.out.println("9. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();
            try {
                switch (ch) {
                    case "1": c.showDashboard(); break;
                    case "2": handleBookAuto(c); break;
                    case "3": handleBookPreferred(c); break;
                    case "4": handleEntrySim(c); break;
                    case "5": handleExitSim(c); break;
                    case "9": back = true; break;
                    default: System.out.println("Invalid");
                }
            } catch (Exception e) {
                printError("Error: %s", e.getMessage());
            }
        }
    }

    private void handleAdmin(Admin a) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View Dashboard");
            System.out.println("2. Add spot(s)");
            System.out.println("3. Generate report");
            System.out.println("9. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1": a.showDashboard(); break;
                case "2":
                    System.out.print("Enter floor number: ");
                    int f = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Enter spot ids (comma separated): ");
                    String s = scanner.nextLine().trim();
                    String[] ids = s.split(",");
                    a.addSpots(f, ids);
                    break;
                case "3": System.out.println(a.generateReport()); break;
                case "9": back = true; break;
                default: System.out.println("Invalid");
            }
        }
    }

    private void handleAttendant(Attendant at) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Attendant Menu ---");
            System.out.println("1. Mark entry");
            System.out.println("2. Mark exit");
            System.out.println("9. Logout");
            System.out.print("Choice: ");
            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1":
                    System.out.print("Vehicle plate: ");
                    String plate = scanner.nextLine().trim();
                    System.out.print("Username: ");
                    String u = scanner.nextLine().trim();
                    ParkingTicket t = new EntryGate(system).processEntry(u, plate);
                    if (t != null) System.out.println("Ticket id: " + t.getTicketId());
                    else System.out.println("Entry failed - no spot");
                    break;
                case "2":
                    System.out.print("Ticket id: ");
                    String tid = scanner.nextLine().trim();
                    try {
                        System.out.print("Enter hours stayed (integer): ");
                        int hours = readHoursFromUser(); // Use validator
                        
                        // 1. Calculate fee using the CLI's billing field
                        Double amt = billing.calculateFeeByHours(hours); 
                        
                        // 2. Process exit
                        new ExitGate(system).processExit(tid, amt);
                        System.out.println("Charge: ₹" + amt);
                    } catch (InvalidHoursException ihe) {
                        System.out.println("Invalid hours: " + ihe.getMessage());
                    } catch (Exception e) {
                        printError("Exit failed: %s", e.getMessage());
                    }
                    break;
                case "9": back = true; break;
                default: System.out.println("Invalid");
            }
        }
    }

    private void handleBookAuto(Customer c) throws Exception {
        System.out.print("Enter vehicle plate: ");
        String plate = scanner.nextLine().trim();
        Vehicle v = new Car(plate, c.getUsername());
        Booking b = system.bookSlot(c, v);
        System.out.println("Booked: " + b.getBookingId() + " for spot " + b.getPreferredSpotId());
    }

    private void handleBookPreferred(Customer c) throws Exception {
        System.out.print("Enter vehicle plate: ");
        String plate = scanner.nextLine().trim();
        System.out.print("Enter preferred spot id: ");
        String pref = scanner.nextLine().trim();
        Vehicle v = new Car(plate, c.getUsername());
        Booking b = system.bookSlot(c, v, pref);
        System.out.println("Booked: " + b.getBookingId() + " pref " + b.getPreferredSpotId());
    }

    private void handleEntrySim(Customer c) {
        System.out.print("Enter vehicle plate: ");
        String plate = scanner.nextLine().trim();
        ParkingTicket t = new EntryGate(system).processEntry(c.getUsername(), plate);
        if (t == null) System.out.println("Entry failed - no spots");
        else System.out.println("Welcome " + c.getName() + "! Ticket: " + t.getTicketId());
    }

    private void handleExitSim(Customer c) {
        System.out.print("Enter ticket id (or blank to auto-find): ");
        String tid = scanner.nextLine().trim();
        if (tid.isEmpty()) {
            ParkingTicket t = system.findOpenTicketForUser(c.getUsername());
            if (t != null) {
                tid = t.getTicketId();
                System.out.println("Auto-found ticket: " + tid);
            } else {
                System.out.println("No open ticket found; please enter ticket id manually.");
                return;
            }
        }
        try {
            System.out.print("Enter hours stayed (integer): ");
            int hours = readHoursFromUser(); // Use validator

            // 1. Calculate fee using the CLI's billing field
            Double amt = billing.calculateFeeByHours(hours);
            
            // 2. Process exit
            new ExitGate(system).processExit(tid, amt);
            System.out.println("Thanks for coming, " + c.getName() + "! Charge: ₹" + amt);
        } catch (InvalidHoursException ihe) {
            System.out.println("Invalid hours: " + ihe.getMessage());
        } catch (Exception e) {
            printError("Exit failed: %s", e.getMessage());
        }
    }
    
    // New helper method to validate hours
    private int readHoursFromUser() throws InvalidHoursException {
        String line = scanner.nextLine().trim();
        try {
            int hours = Integer.parseInt(line);
            if (hours < 0) { // Allow 0 hours, but not negative
                 throw new InvalidHoursException("Hours must be 0 or a positive number.");
            }
            return hours;
        } catch (NumberFormatException e) {
            throw new InvalidHoursException("Enter a valid integer number of hours.");
        }
    }

    private void showLiveStatus() {
        System.out.println(system.getLiveStatus());
    }

    private void showCurrentUsersInside() {
        String[] users = auth.readCurrentUsers();
        System.out.println("Users currently inside (record.txt):");
        if (users.length == 0) System.out.println("(none)");
        for (String u : users) System.out.println("- " + u);
    }

    private void printError(String message, Object... args) {
        String m = String.format(message, args);
        System.err.println(m);
    }
}