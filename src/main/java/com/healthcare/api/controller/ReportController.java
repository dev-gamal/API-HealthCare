package com.healthcare.api.controller;

import com.healthcare.api.service.AppointmentService;
import com.healthcare.api.service.DoctorService;
import com.healthcare.api.service.PatientService;
import com.healthcare.api.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download general system report as PDF")
    public ResponseEntity<InputStreamResource> downloadSystemReport() {

        long patientsCount = patientService.getAllPatients(PageRequest.of(0, 1)).getTotalElements();
        long doctorsCount = doctorService.getAllDoctors(PageRequest.of(0, 1)).getTotalElements();
        long appointmentsCount = appointmentService.getAllAppointments(PageRequest.of(0, 1)).getTotalElements();

        ByteArrayInputStream pdfStream = pdfGeneratorService.generateSimpleReportPdf(patientsCount, doctorsCount, appointmentsCount);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=HealthCare_Rapport.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}