package com.revpasswordmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Master password is required")
    @Size(min = 8, message = "Master password must be at least 8 characters")
    private String masterPassword;

    @NotBlank(message = "Name is required")
    private String name;

    private String phoneNumber;

    @NotBlank(message = "Security question 1 is required")
    private String securityQuestion1;

    @NotBlank(message = "Security answer 1 is required")
    private String securityAnswer1;

    @NotBlank(message = "Security question 2 is required")
    private String securityQuestion2;

    @NotBlank(message = "Security answer 2 is required")
    private String securityAnswer2;

    @NotBlank(message = "Security question 3 is required")
    private String securityQuestion3;

    @NotBlank(message = "Security answer 3 is required")
    private String securityAnswer3;
}
