package com.aarkay.health.service;

import com.aarkay.health.model.Doctor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DoctorService {
    Flux<Doctor> getAllDoctors();
    Mono<Doctor> getDoctorById(Long id);
    Mono<Doctor> createDoctor(Doctor doctor);
    Mono<Doctor> updateDoctor(Long id, Doctor doctor);
    Mono<Void> deleteDoctor(Long id);

    Flux<Doctor> getDoctorsByZipcodeAndSpecialty(String zipcode, String specialty);
}
