package com.aarkay.health.service.impl;

import com.aarkay.health.model.Appointment;
import com.aarkay.health.model.Doctor;
import com.aarkay.health.model.Patient;
import com.aarkay.health.repository.AppointmentRepository;
import com.aarkay.health.repository.DoctorRepository;
import com.aarkay.health.repository.PatientRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;


    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void getAllAppointments() {
        when(appointmentRepository.findAll()).thenReturn(Flux.just(
                new Appointment(1L, LocalDateTime.now(), 1L, 1L),
                new Appointment(2L, LocalDateTime.now(), 2L, 2L),
                new Appointment(3L, LocalDateTime.now(), 3L, 3L),
                new Appointment(4L, LocalDateTime.now(), 4L, 4L)
        ));

        Flux<Appointment> appointmentFlux = appointmentService.getAllAppointments();
        assertNotNull(appointmentFlux);
        assertEquals(4, appointmentFlux.count().block());
    }

    @Test
    void getAppointmentById() {
        Appointment appointment = new Appointment(1L, LocalDateTime.now(), 1L, 1L);
        when(appointmentRepository.findById(1L)).thenReturn(Mono.just(appointment));

        Mono<Appointment> result = appointmentService.getAppointmentById(1L);
        assertEquals(appointment, result.block());
        assertEquals(appointment.getDoctorId(), result.block().getDoctorId());
    }

    @Test
    void createAppointment() {
        Appointment appointment = new Appointment(4L, LocalDateTime.now(), 4L, 4L);
        Doctor doctor = new Doctor(4L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        Patient patient = new Patient(4L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");

        when(patientRepository.findById(4L)).thenReturn(Mono.just(patient));
        when(doctorRepository.findById(4L)).thenReturn(Mono.just(doctor));
        when(appointmentRepository.save(appointment)).thenReturn(Mono.just(appointment));

        Mono<Appointment> result = appointmentService.createAppointment(appointment);
        assertEquals(appointment, result.block());
        assertEquals(appointment.getDoctorId(), result.block().getDoctorId());
        assertEquals(appointment.getPatientId(), result.block().getPatientId());
    }

    @Test
    void updateAppointment() {
        Appointment appointment = new Appointment(4L, LocalDateTime.now(), 4L, 4L);
        Doctor doctor = new Doctor(4L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        Patient patient = new Patient(4L, "Patient", "ZZTest", 25, "zztest@abcpatient.com", "Male", "333-111-1111");

        when(patientRepository.findById(4L)).thenReturn(Mono.just(patient));
        when(doctorRepository.findById(4L)).thenReturn(Mono.just(doctor));
        when(appointmentRepository.findById(4L)).thenReturn(Mono.just(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(Mono.just(appointment));

        Mono<Appointment> result = appointmentService.updateAppointment(4L, appointment);
        assertEquals(appointment.getDoctorId(), result.block().getDoctorId());
        assertEquals(appointment.getPatientId(), result.block().getPatientId());

    }

    @Test
    void deleteAppointment() {
        Appointment appointment = new Appointment(4L, LocalDateTime.now(), 4L, 4L);
        when(appointmentRepository.deleteById(appointment.getId())).thenReturn(Mono.empty());
        Mono<Void> result = appointmentService.deleteAppointment(appointment.getId());
        assertEquals(Mono.empty(), result);
    }
}