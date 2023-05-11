package com.aarkay.health.service;

import com.aarkay.health.model.Appointment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppointmentService {

    Flux<Appointment> getAllAppointments();
    Mono<Appointment> getAppointmentById(Long Id);
    Mono<Appointment> createAppointment(Appointment appointment);
    Mono<Appointment> updateAppointment(Long id, Appointment appointment);
    Mono<Void> deleteAppointment(Long id);
}
