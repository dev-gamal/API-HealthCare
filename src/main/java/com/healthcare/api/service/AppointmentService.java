package com.healthcare.api.service;

import com.healthcare.api.dto.AppointmentRequestDTO;
import com.healthcare.api.dto.AppointmentResponseDTO;
import com.healthcare.api.entity.Appointment;
import com.healthcare.api.entity.AppointmentStatus;
import com.healthcare.api.entity.Doctor;
import com.healthcare.api.entity.Patient;
import com.healthcare.api.mapper.AppointmentMapper;
import com.healthcare.api.repository.AppointmentRepository;
import com.healthcare.api.repository.DoctorRepository;
import com.healthcare.api.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        Appointment appointment = appointmentMapper.toEntity(dto);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.PLANNED);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponseDTO(savedAppointment);
    }

    @Transactional
    @Cacheable(value = "appointments")
    public Page<AppointmentResponseDTO> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(appointmentMapper::toResponseDTO);
    }

    @Transactional
    public Page<AppointmentResponseDTO> getAppointmentByPatient(Long patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable).map(appointmentMapper::toResponseDTO);
    }

    @Transactional
    public Page<AppointmentResponseDTO> getAppointmentByDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(appointmentMapper::toResponseDTO);
    }

    @Transactional
    public Page<AppointmentResponseDTO> searchAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable).map(appointmentMapper::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponseDTO cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found with ID : " + id));
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Can't cancel appointment already completed");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);

        Appointment saveAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponseDTO(saveAppointment);
    }

    @Transactional
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponseDTO updateFullAppointment(Long id, AppointmentRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getStatus() != null) {
            appointment.setStatus(dto.getStatus());
        }

        return appointmentMapper.toResponseDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    @CacheEvict(value = "appointments", allEntries = true)
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));
        appointmentRepository.delete(appointment);
    }
}
