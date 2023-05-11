package com.aarkay.health.service.impl;

import com.aarkay.health.model.Patient;
import com.aarkay.health.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;


    @Test
    void getAllPatients() {
        Flux<Patient> patientFlux = Flux.just(
                new Patient(1L, "John", "Doe", 25, "abcpatient@abcpatient.com", "Male", "111-111-1111"),
                new Patient(2L, "Test", "ZZPatient", 25, "zzpatient@abcpatient.com", "Female", "222-111-1111"),
                new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111")
        );
        when(patientRepository.findAll()).thenReturn(patientFlux);

        Flux<Patient> patients = patientService.getAllPatients();
        assertNotNull(patients);
        assertEquals(3, patients.count().block());
    }

    @Test
    void getPatientById() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientRepository.findById(3L)).thenReturn(Mono.just(patient));
        Mono<Patient> patientMono = patientService.getPatientById(3L);
        assertNotNull(patientMono);
        assertEquals("Patient", patientMono.block().getFirstName());
        assertEquals("ZZTest", patientMono.block().getLastName());
    }

    @Test
    void createPatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientRepository.save(patient)).thenReturn(Mono.just(patient));
        Mono<Patient> patientMono = patientService.createPatient(patient);
        assertNotNull(patientMono);
        assertEquals("Patient", patientMono.block().getFirstName());
        assertEquals("ZZTest", patientMono.block().getLastName());
        assertEquals(25, patientMono.block().getAge());
    }

    @Test
    void updatePatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientRepository.findById(patient.getId())).thenReturn(Mono.just(patient));
        when(patientRepository.save(patient)).thenReturn(Mono.just(patient));
        Mono<Patient> patientMono = patientService.updatePatient(patient.getId(), patient);
        assertNotNull(patientMono);
        assertEquals("Patient", patientMono.block().getFirstName());
        assertEquals("ZZTest", patientMono.block().getLastName());
        assertEquals(25, patientMono.block().getAge());
    }

    @Test
    void deletePatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientRepository.deleteById(patient.getId())).thenReturn(Mono.empty());
        Mono<Void> voidMono = patientService.deletePatient(patient.getId());
        assertNotNull(voidMono);
        assertEquals(voidMono, Mono.empty());
    }
}