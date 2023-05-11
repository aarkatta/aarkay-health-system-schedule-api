package com.aarkay.health.service.impl;

import com.aarkay.health.model.Patient;
import com.aarkay.health.repository.PatientRepository;
import com.aarkay.health.service.PatientService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Flux<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Mono<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    @Override
    public Mono<Patient> createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public Mono<Patient> updatePatient(Long id, Patient patient) {
        return patientRepository.findById(id)
                .flatMap(existingPatient -> {
                    existingPatient.setFirstName(patient.getFirstName());
                    existingPatient.setLastName(patient.getLastName());
                    existingPatient.setEmail(patient.getEmail());
                    existingPatient.setAge(patient.getAge());
                    existingPatient.setGender(patient.getGender());
                    existingPatient.setPhone(patient.getPhone());
                    return patientRepository.save(existingPatient);
                });
    }

    @Override
    public Mono<Void> deletePatient(Long id) {
        return patientRepository.deleteById(id);
    }
}
