package com.healthcare.api.service;

import com.healthcare.api.dto.MedicalFileRequestDTO;
import com.healthcare.api.dto.MedicalFileResponseDTO;
import com.healthcare.api.entity.MedicalFile;
import com.healthcare.api.entity.Patient;
import com.healthcare.api.mapper.MedicalFileMapper;
import com.healthcare.api.repository.MedicalFileRepository;
import com.healthcare.api.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalFileService {
    private final MedicalFileRepository medicalFileRepository;
    private final PatientRepository patientRepository;
    private final MedicalFileMapper medicalFileMapper;

    @Transactional
    @CacheEvict(value = "medicalFiles", allEntries = true)
    public MedicalFileResponseDTO createFile(MedicalFileRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        if (medicalFileRepository.existsByPatientId(patient.getId())) {
            throw new IllegalStateException("This patient already has a medical file");
        }

        MedicalFile file = medicalFileMapper.toEntity(dto);
        file.setPatient(patient);

        MedicalFile savedFile = medicalFileRepository.save(file);
        return medicalFileMapper.toResponseDTO(savedFile);
    }

    @Transactional
    public Page<MedicalFileResponseDTO> getAllMedicalFiles(Pageable pageable) {
        return medicalFileRepository.findAll(pageable)
                .map(medicalFileMapper::toResponseDTO);
    }

    @Transactional
    @Cacheable(value = "medicalFiles", key = "#patientId")
    public MedicalFileResponseDTO getFileByPatientId(Long patientId) {
        MedicalFile file = medicalFileRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("No file were found for this patient ID : " + patientId));
        return medicalFileMapper.toResponseDTO(file);
    }

    @Transactional
    @CacheEvict(value = "medicalFiles", allEntries = true)
    public MedicalFileResponseDTO addObservationAndDiagnosis(Long fileId, String observation, String diagnosis) {
        MedicalFile file = medicalFileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        if (observation != null) {
            file.setObservation(observation);
        }
        if (diagnosis != null) {
            file.setDiagnosis(diagnosis);
        }
        return medicalFileMapper.toResponseDTO(medicalFileRepository.save(file));
    }

    @Transactional
    @CacheEvict(value = "medicalFiles", allEntries = true)
    public MedicalFileResponseDTO updateMedicalFile(Long id, MedicalFileRequestDTO dto) {
        MedicalFile file = medicalFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical file not found with ID: " + id));

        if (dto.getDiagnosis() != null) {
            file.setDiagnosis(dto.getDiagnosis());
        }
        if (dto.getObservation() != null) {
            file.setObservation(dto.getObservation());
        }

        return medicalFileMapper.toResponseDTO(medicalFileRepository.save(file));
    }

    @Transactional
    @CacheEvict(value = "medicalFiles", allEntries = true)
    public void deleteMedicalFile(Long id) {
        MedicalFile file = medicalFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical file not found with ID: " + id));
        medicalFileRepository.delete(file);
    }
}
