package com.healthcare.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDTO {

    @NotBlank(message = "The username is mandatory")
    @Size(min = 3, max = 20, message = "The username must contain 3 to 20 characters")
    private String username;

    @NotBlank(message = "The password is mandatory")
    @Size(min = 6, message = "The password must contain at least 6 characters")
    private String password;

    @NotBlank(message = "The last name is mandatory")
    private String lastName;

    @NotBlank(message = "The first name is mandatory")
    private String firstName;

    @Email(message = "Email invalid")
    @NotBlank(message = "The email is mandatory")
    private String email;

    @NotBlank(message = "The phone number is mandatory")
    private String phone;

    @NotNull(message = "The date of birth is mandatory")
    @Past(message = "The date of birth must be in the past")
    private LocalDate birthDate;
}
