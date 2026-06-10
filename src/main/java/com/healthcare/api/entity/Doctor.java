package com.healthcare.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "doctor")
@PrimaryKeyJoinColumn(name = "id")
@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class Doctor extends User {

    private String name;
    private String specialty;
    private String phone;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointmentList;
}
