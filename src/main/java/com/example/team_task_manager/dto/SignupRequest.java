package com.example.team_task_manager.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be 6–40 characters")
    private String password;

    @Size(max = 100)
    private String fullName;

    // Optional: "ROLE_ADMIN" or "ROLE_MEMBER" — defaults to ROLE_MEMBER
    private String role;
}
