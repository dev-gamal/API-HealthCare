package com.healthcare.api.service;

import com.healthcare.api.dto.DoctorRequestDTO;
import com.healthcare.api.dto.DoctorResponseDTO;
import com.healthcare.api.dto.PatientRequestDTO;
import com.healthcare.api.dto.PatientResponseDTO;
import com.healthcare.api.entity.Doctor;
import com.healthcare.api.entity.Patient;
import com.healthcare.api.entity.Roles;
import com.healthcare.api.mapper.DoctorMapper;
import com.healthcare.api.repository.DoctorRepository;
import com.healthcare.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("A user with this username already exists!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("A user with this email already exists!");
        }

        Doctor doctor = doctorMapper.toEntity(dto);

        doctor.setUsername(dto.getUsername());
        doctor.setEmail(dto.getEmail());
        doctor.setPassword(passwordEncoder.encode(dto.getPassword()));
        doctor.setRole(Roles.DOCTOR);

        Doctor saveDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(saveDoctor);
    }

    @Transactional
    @Cacheable(value = "doctors")
    public Page<DoctorResponseDTO> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(doctorMapper::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("This doctor doesn't exist"));

        doctor.setName(dto.getName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setPhone(dto.getPhone());

        if (!doctor.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("This email is already in use by another user!");
            }
            doctor.setEmail(dto.getEmail());
        }

        Doctor updateDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(updateDoctor);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Doctor not found with the ID : " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional
    public Page<DoctorResponseDTO> findBySpecialty(String specialty, Pageable pageable) {
        return doctorRepository.findBySpecialtyContainingIgnoreCase(specialty, pageable)
                .map(doctorMapper::toResponseDTO);
    }
}
