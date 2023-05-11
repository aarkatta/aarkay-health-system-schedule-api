package com.aarkay.health.controller;

import com.aarkay.health.model.Doctor;
import com.aarkay.health.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private WebTestClient   webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToController(doctorController).build();
    }

    @Test
    void getAllDoctors() {
        when(doctorService.getAllDoctors()).thenReturn(Flux.just(
                new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "abc@abcdoctor.com"),
                new Doctor(2L, "Jane", "Doe", "Orthopedics", "212-212-2255", "12345", "def@defdoctor.com"),
                new Doctor(3L, "John", "Smith", "Internal Medicine", "212-212-5522", "12345", "ghi@ghidoctor.com"),
                new Doctor(4L, "Jane", "Smith", "Pediatrics", "558-885-8888", "12345", "jkl@jkldoctor.com")
        ));

        webTestClient.get()
                .uri("/doctors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Doctor.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    assertEquals(4, response.getResponseBody().size());
                });
    }

    @Test
    void getDoctorById() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "abc@abcdoctor.com");
        when(doctorService.getDoctorById(any(Long.class))).thenReturn(Mono.just(doctor));

        webTestClient.get()
                .uri("/doctors/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Doctor.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    assertEquals(doctor, response.getResponseBody());
                });
    }

    @Test
    void getDoctorByZipCodeAndSpecialty() {
        when(doctorService.getDoctorsByZipcodeAndSpecialty(any(String.class), any(String.class))).thenReturn(Flux.just(
                new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "abc@abcdoctor.com"),
                new Doctor(2L, "Jane", "Doe", "Orthopedics", "212-212-2255", "12345", "def@defdoctor.com"),
                new Doctor(3L, "John", "Smith", "Internal Medicine", "212-212-5522", "12345", "ghi@ghidoctor.com")
        ));

        webTestClient.get()
                .uri("/doctors/search?zipcode=12345&specialty=Cardiology")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Doctor.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    assertEquals(3, response.getResponseBody().size());
                });

    }

    @Test
    void createDoctor() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "def@defdoctor.com");
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(Mono.just(doctor));

        webTestClient.post()
                .uri("/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(doctor), Doctor.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Doctor.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    assertEquals(doctor, response.getResponseBody());
                });

    }

    @Test
    void updateDoctor() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "ghi@ghidoctor.com");
        when(doctorService.updateDoctor(eq(1L), any(Doctor.class))).thenReturn(Mono.just(doctor));

        webTestClient.put()
                .uri("/doctors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(doctor), Doctor.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Doctor.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                    assertEquals(doctor, response.getResponseBody());
                });
    }

    @Test
    void deleteDoctor() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        when(doctorService.deleteDoctor(doctor.getId())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/doctors/{id}", doctor.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Doctor.class)
                .consumeWith(response -> {
                    assertNull(response.getResponseBody());
                });

    }
}