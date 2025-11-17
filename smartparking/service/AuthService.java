package service;

import main.User;
import main.Admin;
import main.Attendant;
import main.Customer;
import util.Constants;
import exceptions.UserAlreadyExistsException;

import java.io.*;
import java.util.Arrays;

/**
 * AuthService - New Model:
 * - Registered users are stored persistently in 'record.txt'.
 * - 'record.txt' is read on login/registration to find users.
 * - Current users (inside the lot) are stored in a fixed-size in-memory array.
 * - WARNING: Current user data is lost if the application stops.
 */
public class AuthService {
    private final String recordFilePath;
    private String[] currentUsers;    // In-memory array for users inside
    private int currentUserCount;

    /**
     * Initializes the AuthService.
     * @param recordFilePath The path to the file storing registered users.
     * @param totalSpotCount The total number of spots, used to size the currentUsers array.
     */
    public AuthService(String recordFilePath, int totalSpotCount) {
        this.recordFilePath = recordFilePath;
        // Array size is fixed to total number of parking spots, as requested.
        this.currentUsers = new String[totalSpotCount];
        this.currentUserCount = 0;
    }

    public String getRecordFilePath() { return recordFilePath; }

    /**
     * Registers a new user by writing their details to the record.txt file.
     * Throws UserAlreadyExistsException if username already exists in the file.
     */
    public boolean registerUser(User u) throws UserAlreadyExistsException {
        if (u == null) return false;

        // This is now a slow operation, as it reads the file.
        if (findUserByUsername(u.getUsername()) != null) {
            throw new UserAlreadyExistsException("Username already exists: " + u.getUsername());
        }

        // Get balance if customer, default to 0.0
        double balance = 0.0;
        if (u instanceof Customer) {
            balance = ((Customer) u).getAccount().getBalance();
        }

        // Format: username;hashedPassword;role;name;email;balance
        String line = String.join(";",
            u.getUsername(),
            u.getHashedPassword(),
            u.getRole(),
            u.getName(),
            u.getEmail(),
            String.valueOf(balance)
        );

        // Append the new user to the file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(recordFilePath, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to record file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds a user by reading the record.txt file line by line.
     * This is a slow operation, performed on login and registration.
     * @return User object if found, null otherwise.
     */
    public User findUserByUsername(String username) {
        if (username == null) return null;
        File f = new File(recordFilePath);
        if (!f.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // username;hashedPassword;role;name;email;balance
                String[] parts = line.split(";", 6); 
                if (parts.length < 6) continue; // Skip malformed lines

                String fileUsername = parts[0];

                if (fileUsername.equals(username)) {
                    String hashedPass = parts[1];
                    String role = parts[2];
                    String name = parts[3];
                    String email = parts[4];
                    double balance = Double.parseDouble(parts[5]);

                    // Re-create the correct user object based on role
                    switch (role) {
                        case Constants.ROLE_ADMIN:
                            return new Admin(username, hashedPass, name, email);
                        case Constants.ROLE_ATTENDANT:
                            return new Attendant(username, hashedPass, name, email);
                        case Constants.ROLE_CUSTOMER:
                            // Note: Vehicle plates are not persisted in this model.
                            return new Customer(username, hashedPass, name, email, balance);
                        default:
                            return null; // Unknown role
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading record file: " + e.getMessage());
        }
        return null; // Not found
    }

    /**
     * Authenticate user by finding them in record.txt and checking password.
     */
    public User login(String username, String password) {
        User u = findUserByUsername(username); // Slow file read
        if (u == null) return null;
        if (!u.checkPassword(password)) return null;
        return u;
    }

    /**
     * Adds a username to the in-memory array of current users.
     */
    public void addCurrentUser(String username) {
        if (currentUserCount < currentUsers.length) {
            currentUsers[currentUserCount++] = username;
        } else {
            System.err.println("Error: Current user array is full. Cannot add user.");
        }
    }

    /**
     * Removes a username from the in-memory array of current users.
     * This involves shifting array elements to the left.
     */
    public void removeCurrentUser(String username) {
        int foundIndex = -1;
        for (int i = 0; i < currentUserCount; i++) {
            if (currentUsers[i] != null && currentUsers[i].equals(username)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1) {
            // Shift all elements to the left
            for (int i = foundIndex; i < currentUserCount - 1; i++) {
                currentUsers[i] = currentUsers[i + 1];
            }
            currentUsers[currentUserCount - 1] = null; // Clear the last slot
            currentUserCount--;
        }
    }

    /**
     * Reads all current users from the in-memory array.
     */
    public String[] readCurrentUsers() {
        // Return a clean copy, trimmed to the actual count
        return Arrays.copyOf(currentUsers, currentUserCount);
    }
}
