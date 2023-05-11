package com.aarkay.health.service;

import com.aarkay.health.model.Patient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PatientService {
    Flux<Patient> getAllPatients();
    Mono<Patient> getPatientById(Long id);
    Mono<Patient> createPatient(Patient patient);
    Mono<Patient> updatePatient(Long id, Patient patient);
    Mono<Void> deletePatient(Long id);
}
