package service;

import main.User;
import exceptions.UserAlreadyExistsException;

import java.io.*;
import java.util.ArrayList;

/**
 * AuthService - simplified and updated:
 * - Registered users stored in a resizable array (grows by doubling capacity).
 * - record.txt is the only persistent file (stores usernames currently inside).
 */
public class AuthService {
    private final String recordFilePath;
    private User[] registeredUsers;    // now resizable (not final)
    private int registeredCount;

    public AuthService(String recordFilePath) {
        this.recordFilePath = recordFilePath;
        this.registeredUsers = new User[500]; // initial capacity
        this.registeredCount = 0;
    }

    public String getRecordFilePath() { return recordFilePath; }

    /**
     * Register user into in-memory array.
     * Throws UserAlreadyExistsException if username already exists.
     * Automatically grows the internal array when capacity reached.
     */
    public boolean registerUser(User u) throws UserAlreadyExistsException {
        if (u == null) return false;

        // duplicate check
        if (findUserByUsername(u.getUsername()) != null) {
            throw new UserAlreadyExistsException("Username already exists: " + u.getUsername());
        }

        // ensure capacity (grow if needed)
        ensureCapacity();

        // add user
        registeredUsers[registeredCount++] = u;
        return true;
    }

    // Grow internal array by doubling its size
    private void ensureCapacity() {
        if (registeredCount < registeredUsers.length) return;
        int newSize = registeredUsers.length * 2;
        User[] newArr = new User[newSize];
        for (int i = 0; i < registeredUsers.length; i++) newArr[i] = registeredUsers[i];
        registeredUsers = newArr;
    }

    /**
     * Linear search for user in the internal array.
     */
    public User findUserByUsername(String username) {
        if (username == null) return null;
        for (int i = 0; i < registeredCount; i++) {
            User u = registeredUsers[i];
            if (u != null && u.getUsername().equals(username)) return u;
        }
        return null;
    }

    /**
     * Authenticate user by username & password.
     */
    public User login(String username, String password) {
        User u = findUserByUsername(username);
        if (u == null) return null;
        if (!u.checkPassword(password)) return null;
        return u;
    }

    // add username to record.txt (append)
    public void addCurrentUser(String username) {
        File f = new File(recordFilePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
            bw.write(username.trim());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to record file: " + e.getMessage());
        }
    }

    // remove username from record.txt (rewrite skipping the username)
    public void removeCurrentUser(String username) {
        File input = new File(recordFilePath);
        File temp = new File(recordFilePath + ".tmp");
        if (!input.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(input));
             BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(username)) continue;
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating record file: " + e.getMessage());
            return;
        }
        if (temp.exists()) {
            input.delete();
            temp.renameTo(input);
        }
    }

    // read all current users from record.txt
    public String[] readCurrentUsers() {
        File f = new File(recordFilePath);
        if (!f.exists()) return new String[0];
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) list.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Error reading record file: " + e.getMessage());
        }
        return list.toArray(new String[0]);
    }
}
