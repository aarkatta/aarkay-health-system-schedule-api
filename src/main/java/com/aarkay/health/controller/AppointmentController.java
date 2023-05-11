package com.aarkay.health.controller;

import com.aarkay.health.model.Appointment;
import com.aarkay.health.service.AppointmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public Flux<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Appointment>> getAppointmentById(@PathVariable("id") Long id) {
        return appointmentService.getAppointmentById(id)
                .map(appointment -> ResponseEntity.ok(appointment))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.createAppointment(appointment);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Appointment>> updateAppointment(@PathVariable("id") Long id, @RequestBody Appointment appointment) {
        return appointmentService.updateAppointment(id, appointment)
                .map(updatedAppointment -> ResponseEntity.ok(updatedAppointment))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAppointment(@PathVariable("id") Long id) {
        return appointmentService.deleteAppointment(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
