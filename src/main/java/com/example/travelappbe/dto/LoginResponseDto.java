package com.example.travelappbe.dto;

public class LoginResponseDto {

    private String token;

    // Constructors
    public LoginResponseDto() {
    }

    public LoginResponseDto(String token) {
        this.token = token;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResponseDto{" +
                "token='" + token + '\'' +
                '}';
    }
}
