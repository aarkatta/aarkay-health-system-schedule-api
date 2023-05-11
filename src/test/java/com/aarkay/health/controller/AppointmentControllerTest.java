package com.aarkay.health.controller;

import com.aarkay.health.model.Appointment;
import com.aarkay.health.service.AppointmentService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(appointmentController).build();
    }


    @Test
    public void testGetAllAppointments() {
        Flux<Appointment> appointmentFlux = Flux.just(
                new Appointment(1L, LocalDateTime.now(), 1L, 1L),
                new Appointment(2L, LocalDateTime.now(), 2L, 2L),
                new Appointment(3L, LocalDateTime.now(), 3L, 3L),
                new Appointment(4L, LocalDateTime.now(), 4L, 4L)
        );

        when(appointmentService.getAllAppointments()).thenReturn(appointmentFlux);

        webTestClient.get()
                .uri("/appointments")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Appointment>>() {})
                .consumeWith(response -> {
                    List<Appointment> appointments = response.getResponseBody();
                    assertNotNull(appointments);
                    assertEquals(4, appointments.size());
                });

    }

    @Test
    void getAppointmentById() {
        Appointment appointment = new Appointment(1L, LocalDateTime.now(), 1L, 1L);
        when(appointmentService.getAppointmentById(appointment.getId())).thenReturn(Mono/*just*/.just(appointment));

        webTestClient.get()
                .uri("/appointments/{id}", appointment.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Appointment.class)
                .consumeWith(response -> {
                    List<Appointment> appointments = response.getResponseBody();
                    assertNotNull(appointments);
                    assertEquals(1, appointments.size());
                });
    }

    @Test
    void createAppointment() {
        Appointment appointment = new Appointment(1L, LocalDateTime.now(), 1L, 1L);
        when(appointmentService.createAppointment(any(Appointment.class))).thenReturn(Mono.just(appointment));

        webTestClient.post()
                .uri("/appointments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(appointment), Appointment.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Appointment.class)
                .consumeWith(response -> {
                    Appointment createdAppointment = response.getResponseBody();
                    assertNotNull(createdAppointment);
                    assertEquals(1L, createdAppointment.getId());
                });

    }

    @Test
    void updateAppointment() {
        Appointment appointment = new Appointment(1L, LocalDateTime.now(), 1L, 1L);
        when(appointmentService.updateAppointment(eq(appointment.getId()), any(Appointment.class))).thenReturn(Mono.just(appointment));

        webTestClient.put()
                .uri("/appointments/{id}", appointment.getId())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(appointment), Appointment.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Appointment.class)
                .consumeWith(response -> {
                    Appointment updatedAppointment = response.getResponseBody();
                    assertNotNull(updatedAppointment);
                    assertEquals(1L, updatedAppointment.getId());
                });
    }

    @Test
    void deleteAppointment() {
        Appointment appointment = new Appointment(1L, LocalDateTime.now(), 1L, 1L);
        when(appointmentService.deleteAppointment(appointment.getId())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/appointments/{id}", appointment.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Appointment.class)
                .consumeWith(response -> {
                    Appointment deletedAppointment = response.getResponseBody();
                    assertNull(deletedAppointment);
                });
    }
}