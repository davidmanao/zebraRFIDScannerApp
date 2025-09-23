package com.mixtra.zebrarfidscannerapp.api.model;

public class LoginRequest {
    private String email;
    private String password;
    
    public LoginRequest(String username, String password) {
        this.email = username;
        this.password = password;
    }
    
    public String getUsername() {
        return email;
    }
    
    public void setUsername(String username) {
        this.email = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}