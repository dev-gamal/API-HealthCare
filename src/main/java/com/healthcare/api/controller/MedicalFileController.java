package com.healthcare.api.controller;

import com.healthcare.api.dto.MedicalFileRequestDTO;
import com.healthcare.api.dto.MedicalFileResponseDTO;
import com.healthcare.api.service.AppointmentService;
import com.healthcare.api.service.MedicalFileService;
import com.healthcare.api.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class MedicalFileController {
    private final MedicalFileService medicalFileService;
    private final PdfGeneratorService pdfGeneratorService;
    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new medical file for patient")
    public ResponseEntity<MedicalFileResponseDTO> createMedicalFile(@Valid @RequestBody MedicalFileRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalFileService.createFile(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN')")
    @Operation(summary = "Get all medical files")
    public ResponseEntity<Page<MedicalFileResponseDTO>> getAllMedicalFiles(
            @PageableDefault(size = 10, sort = "creationDate") Pageable pageable) {
        return ResponseEntity.ok(medicalFileService.getAllMedicalFiles(pageable));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN', 'PATIENT')")
    @Operation(summary = "Get file by patient ID")
    public ResponseEntity<MedicalFileResponseDTO> getMedicalFileByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalFileService.getFileByPatientId(patientId));
    }

    @PatchMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN')")
    @Operation(summary = "Add diagnosis or observation")
    public ResponseEntity<MedicalFileResponseDTO> updateNotes(
            @PathVariable Long id,
            @RequestParam String observation,
            @RequestParam String diagnosis) {
        return ResponseEntity.ok(medicalFileService.addObservationAndDiagnosis(id, observation, diagnosis));
    }

    @GetMapping("/{patientId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN', 'PATIENT')")
    @Operation(summary = "Download medical file as PDF")
    public ResponseEntity<InputStreamResource> downloadMedicalFilePdf(@PathVariable Long patientId) {
        MedicalFileResponseDTO medicalFile = medicalFileService.getFileByPatientId(patientId);

        ByteArrayInputStream pdfStream = pdfGeneratorService.generateMedicalFilePdf(medicalFile);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=dossier_medical_patient_" + patientId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
