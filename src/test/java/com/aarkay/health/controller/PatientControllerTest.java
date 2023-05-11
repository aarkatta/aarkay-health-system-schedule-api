package com.aarkay.health.controller;

import com.aarkay.health.model.Patient;
import com.aarkay.health.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(patientController).build();
    }

    @Test
    void getAllPatients() {
        Flux<Patient> patientFlux = Flux.just(
                new Patient(1L, "John", "Doe", 25, "abcpatient@abcpatient.com", "Male", "111-111-1111"),
                new Patient(2L, "Test", "ZZPatient", 25, "zzpatient@abcpatient.com", "Female", "222-111-1111"),
                new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111")
        );
        when(patientService.getAllPatients()).thenReturn(patientFlux);

        webTestClient.get()
                .uri("/patients")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Patient>>() {})
                .consumeWith(response -> {
                    List<Patient> patients = response.getResponseBody();
                    assertNotNull(patients);
                    assertEquals(3, patients.size());
                });
    }

    @Test
    void getPatientById() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientService.getPatientById(3L)).thenReturn(Mono.just(patient));

        webTestClient.get()
                .uri("/patients/3")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Patient.class)
                .consumeWith(response -> {
                    Patient patientResponse = response.getResponseBody();
                    assertNotNull(patientResponse);
                    assertEquals(3L, patientResponse.getId());
                    assertEquals("Patient", patientResponse.getFirstName());
                    assertEquals("ZZTest", patientResponse.getLastName());
                    assertEquals(25, patientResponse.getAge());
                });
    }

    @Test
    void createPatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientService.createPatient(patient)).thenReturn(Mono.just(patient));

        webTestClient.post()
                .uri("/patients")
                .body(Mono.just(patient), Patient.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Patient.class)
                .consumeWith(response -> {
                    Patient patientResponse = response.getResponseBody();
                    assertNotNull(patientResponse);
                    assertEquals(3L, patientResponse.getId());
                    assertEquals("Patient", patientResponse.getFirstName());
                    assertEquals("ZZTest", patientResponse.getLastName());
                    assertEquals(25, patientResponse.getAge());
                });

    }

    @Test
    void updatePatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientService.updatePatient(3L, patient)).thenReturn(Mono.just(patient));

        webTestClient.put()
                .uri("/patients/3")
                .body(Mono.just(patient), Patient.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Patient.class)
                .consumeWith(response -> {
                    Patient patientResponse = response.getResponseBody();
                    assertNotNull(patientResponse);
                    assertEquals(3L, patientResponse.getId());
                    assertEquals("Patient", patientResponse.getFirstName());
                    assertEquals("ZZTest", patientResponse.getLastName());
                    assertEquals(25, patientResponse.getAge());
                });
    }

    @Test
    void deletePatient() {
        Patient patient = new Patient(3L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");
        when(patientService.deletePatient(3L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/patients/3")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Patient.class)
                .consumeWith(response -> {
                    Patient patientResponse = response.getResponseBody();
                    assertNull(patientResponse);
                });

    }
}