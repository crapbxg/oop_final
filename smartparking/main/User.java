package main;

import util.AuthUtil;

public abstract class User {
    protected String username;
    protected String hashedPassword;
    protected String name;
    protected String email;
    protected String role;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public User(String username, String hashedPassword, String name, String email, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public abstract void showDashboard();

    public boolean checkPassword(String password) {
        return hashedPassword.equals(AuthUtil.hash(password));
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getEmail() {
        return email;
    }
}