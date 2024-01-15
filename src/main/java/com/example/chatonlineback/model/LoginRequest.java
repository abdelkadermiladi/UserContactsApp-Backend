package com.example.chatonlineback.model;

public class LoginRequest {
    private String username;
    private String password;

    // Getters and setters (you can use Lombok for convenience)

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
