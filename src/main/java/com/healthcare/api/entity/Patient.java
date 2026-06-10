package com.healthcare.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patient")
@PrimaryKeyJoinColumn(name = "id")
@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class Patient extends User {

    private String lastName;
    private String firstName;
    private String phone;
    private LocalDate birthDate;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointmentList;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private MedicalFile medicalFile;
}
