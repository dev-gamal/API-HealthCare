package com.healthcare.api.controller;

import com.healthcare.api.dto.*;
import com.healthcare.api.entity.AppointmentStatus;
import com.healthcare.api.service.AppointmentService;
import com.healthcare.api.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final PdfGeneratorService pdfGeneratorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Create new appointment for patient")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all appointments")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAllAppointments(
            @PageableDefault(size = 10, sort = "appointmentDate")Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(pageable));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN')")
    @Operation(summary = "Search appointments by doctor")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "appointmentDate") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointmentByDoctor(doctorId, pageable));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Search appointments by patient")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "appointmentDate") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAppointmentByPatient(patientId, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN')")
    @Operation(summary = "Search appointments by status")
    public ResponseEntity<Page<AppointmentResponseDTO>> searchByStatus(
            @RequestParam AppointmentStatus status,
            @PageableDefault(size = 10, sort = "appointmentDate") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.searchAppointmentsByStatus(status, pageable));
    }

    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN')")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointment(id, status);
        return ResponseEntity.ok(updatedAppointment);
    }

    @GetMapping("/patient/{patientId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN', 'PATIENT')")
    @Operation(summary = "Download appointments list as PDF")
    public ResponseEntity<InputStreamResource> downloadPatientAppointments(@PathVariable Long patientId) {
        Page<AppointmentResponseDTO> appointmentsPage = appointmentService.getAppointmentByPatient(patientId, PageRequest.of(0, 1000));
        List<AppointmentResponseDTO> appointments = appointmentsPage.getContent();

        String patientName = appointments.isEmpty() ? "Inconnu" : appointments.get(0).getPatientCompleteName();
        ByteArrayInputStream pdfStream = pdfGeneratorService.generatePatientAppointmentsPdf(appointments, patientName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=rendezvous_patient_" + patientId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}