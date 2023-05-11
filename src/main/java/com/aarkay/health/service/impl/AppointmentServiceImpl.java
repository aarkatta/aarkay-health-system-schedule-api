package com.aarkay.health.service.impl;

import com.aarkay.health.model.Appointment;
import com.aarkay.health.model.Doctor;
import com.aarkay.health.model.Patient;
import com.aarkay.health.repository.AppointmentRepository;
import com.aarkay.health.repository.DoctorRepository;
import com.aarkay.health.repository.PatientRepository;
import com.aarkay.health.service.AppointmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public Flux<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Mono<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    public Mono<Appointment> createAppointment(Appointment appointment) {
        return Mono.zip(
                        doctorRepository.findById(appointment.getDoctorId()),
                        patientRepository.findById(appointment.getPatientId()))
                .flatMap(tuple -> {
                    Doctor doctor = tuple.getT1();
                    Patient patient = tuple.getT2();
                    appointment.setDoctorId(doctor.getId());
                    appointment.setPatientId(patient.getId());
                    return appointmentRepository.save(appointment);
                });
    }

    @Override
    public Mono<Appointment> updateAppointment(Long id, Appointment appointment) {
        return appointmentRepository.findById(id)
                .flatMap(existingAppointment -> {
                    existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
                    return Mono.zip(
                            doctorRepository.findById(appointment.getDoctorId()),
                            patientRepository.findById(appointment.getPatientId()))
                            .map(tuple -> {
                                Doctor doctor = tuple.getT1();
                                Patient patient = tuple.getT2();
                                existingAppointment.setPatientId(patient.getId());
                                existingAppointment.setDoctorId(doctor.getId());
                                return existingAppointment;
                            });
                })
                .flatMap(appointmentRepository::save);
    }

    @Override
    public Mono<Void> deleteAppointment(Long id) {
        return appointmentRepository.deleteById(id);
    }
}
