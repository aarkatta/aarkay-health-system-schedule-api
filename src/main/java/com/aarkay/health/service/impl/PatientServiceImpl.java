package com.aarkay.health.service.impl;

import com.aarkay.health.fhir.client.PatientFhirClient;
import com.aarkay.health.model.Patient;
import com.aarkay.health.service.PatientService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientFhirClient patientFhirClient;

    public PatientServiceImpl(PatientFhirClient patientFhirClient) {
        this.patientFhirClient = patientFhirClient;
    }

    @Override
    public Flux<Patient> getAllPatients() {
        return patientFhirClient.findAll();
    }

    @Override
    public Mono<Patient> getPatientById(Long id) {
        return patientFhirClient.findById(id);
    }

    @Override
    public Mono<Patient> createPatient(Patient patient) {
        return patientFhirClient.create(patient);
    }

    @Override
    public Mono<Patient> updatePatient(Long id, Patient patient) {
        return patientFhirClient.findById(id)
                .flatMap(existingPatient -> {
                    existingPatient.setFirstName(patient.getFirstName());
                    existingPatient.setLastName(patient.getLastName());
                    existingPatient.setEmail(patient.getEmail());
                    existingPatient.setAge(patient.getAge());
                    existingPatient.setGender(patient.getGender());
                    existingPatient.setPhone(patient.getPhone());
                    return patientFhirClient.update(id, existingPatient);
                });
    }

    @Override
    public Mono<Void> deletePatient(Long id) {
        return patientFhirClient.delete(id);
    }
}
