package com.example.demo.dto;

public record AuthResponse(
        String token,
        String type,
        AccountResponse user) {

    public AuthResponse(String token, AccountResponse user) {
        this(token, "Bearer", user);
    }
}
