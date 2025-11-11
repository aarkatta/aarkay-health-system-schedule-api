package com.aarkay.health.service.impl;

import com.aarkay.health.fhir.client.AppointmentFhirClient;
import com.aarkay.health.fhir.client.DoctorFhirClient;
import com.aarkay.health.fhir.client.PatientFhirClient;
import com.aarkay.health.model.Appointment;
import com.aarkay.health.model.Doctor;
import com.aarkay.health.model.Patient;
import com.aarkay.health.service.AppointmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentFhirClient appointmentFhirClient;
    private final DoctorFhirClient doctorFhirClient;
    private final PatientFhirClient patientFhirClient;

    public AppointmentServiceImpl(AppointmentFhirClient appointmentFhirClient, DoctorFhirClient doctorFhirClient, PatientFhirClient patientFhirClient) {
        this.appointmentFhirClient = appointmentFhirClient;
        this.doctorFhirClient = doctorFhirClient;
        this.patientFhirClient = patientFhirClient;
    }

    @Override
    public Flux<Appointment> getAllAppointments() {
        return appointmentFhirClient.findAll();
    }

    @Override
    public Mono<Appointment> getAppointmentById(Long id) {
        return appointmentFhirClient.findById(id);
    }

    @Override
    public Mono<Appointment> createAppointment(Appointment appointment) {
        return Mono.zip(
                        doctorFhirClient.findById(appointment.getDoctorId()),
                        patientFhirClient.findById(appointment.getPatientId()))
                .flatMap(tuple -> {
                    Doctor doctor = tuple.getT1();
                    Patient patient = tuple.getT2();
                    appointment.setDoctorId(doctor.getId());
                    appointment.setPatientId(patient.getId());
                    return appointmentFhirClient.create(appointment);
                });
    }

    @Override
    public Mono<Appointment> updateAppointment(Long id, Appointment appointment) {
        return appointmentFhirClient.findById(id)
                .flatMap(existingAppointment -> {
                    existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
                    return Mono.zip(
                            doctorFhirClient.findById(appointment.getDoctorId()),
                            patientFhirClient.findById(appointment.getPatientId()))
                            .map(tuple -> {
                                Doctor doctor = tuple.getT1();
                                Patient patient = tuple.getT2();
                                existingAppointment.setPatientId(patient.getId());
                                existingAppointment.setDoctorId(doctor.getId());
                                return existingAppointment;
                            });
                })
                .flatMap(updatedAppointment -> appointmentFhirClient.update(id, updatedAppointment));
    }

    @Override
    public Mono<Void> deleteAppointment(Long id) {
        return appointmentFhirClient.delete(id);
    }
}
