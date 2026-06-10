package com.healthcare.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorRequestDTO {

    @NotBlank(message = "The username is mandatory")
    @Size(min = 3, max = 20, message = "The username must contain 3 to 20 characters")
    private String username;

    @NotBlank(message = "The password is mandatory")
    @Size(min = 6, message = "The password must contain at least 6 characters")
    private String password;

    @NotBlank(message = "The doctor name is mandatory")
    private String name;

    @NotBlank(message = "The specialty is mandatory")
    private String specialty;

    @Email(message = "Email format is invalid")
    @NotBlank(message = "The email is mandatory")
    private String email;

    @NotBlank(message = "The phone number is mandatory")
    private String phone;
}
