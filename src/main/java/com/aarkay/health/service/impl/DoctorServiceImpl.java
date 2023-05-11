package com.aarkay.health.service.impl;

import com.aarkay.health.model.Doctor;
import com.aarkay.health.repository.DoctorRepository;
import com.aarkay.health.service.DoctorService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Flux<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Mono<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    @Override
    public Mono<Doctor> createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public Mono<Doctor> updateDoctor(Long id, Doctor doctor) {
        return doctorRepository.findById(id)
                .flatMap(existingDoctor -> {
                    existingDoctor.setFirstName(doctor.getFirstName());
                    existingDoctor.setLastName(doctor.getLastName());
                    existingDoctor.setSpecialty(doctor.getSpecialty());
                    existingDoctor.setZipcode(doctor.getZipcode());
                    existingDoctor.setPhone(doctor.getPhone());
                    existingDoctor.setEmail(doctor.getEmail());
                    return doctorRepository.save(existingDoctor);
                });
    }

    @Override
    public Mono<Void> deleteDoctor(Long id) {
        return doctorRepository.deleteById(id);
    }

    @Override
    public Flux<Doctor> getDoctorsByZipcodeAndSpecialty(String zipcode, String specialty) {
        return doctorRepository.findByZipcodeAndSpecialty(zipcode, specialty);
    }
}
