package com.example.travelappbe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequestDto {
    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Adresa de email trebuie să fie validă")
    private String email;

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }
}