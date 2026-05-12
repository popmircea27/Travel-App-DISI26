package com.example.travelappbe.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequestDto {
    
    @NotBlank(message = "Token-ul este obligatoriu")
    private String token;

    @NotBlank(message = "Noua parolă este obligatorie")
    private String newPassword;

    public String getToken() { 
        return token; 
    }
    public void setToken(String token) { 
        this.token = token; 
    }
    public String getNewPassword() { 
        return newPassword; 
    }
    public void setNewPassword(String newPassword) { 
        this.newPassword = newPassword; 
    }
}