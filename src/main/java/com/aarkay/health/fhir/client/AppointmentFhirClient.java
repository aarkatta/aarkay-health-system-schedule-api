package com.aarkay.health.fhir.client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.aarkay.health.exception.FhirClientException;
import com.aarkay.health.mapper.AppointmentMapper;
import com.aarkay.health.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FHIR client for Appointment resource operations.
 * Provides reactive methods to interact with FHIR Appointment API.
 */
@Component
public class AppointmentFhirClient {

    private final IGenericClient fhirClient;
    private final AppointmentMapper appointmentMapper;

    public AppointmentFhirClient(IGenericClient fhirClient, AppointmentMapper appointmentMapper) {
        this.fhirClient = fhirClient;
        this.appointmentMapper = appointmentMapper;
    }

    /**
     * Creates a new appointment in the FHIR server.
     * Validates that both doctor and patient exist before creation.
     *
     * @param appointment Appointment entity to create
     * @return Mono containing the created appointment with ID
     */
    public Mono<Appointment> create(Appointment appointment) {
        return Mono.fromCallable(() -> {
            try {
                // Validate doctor exists
                Practitioner doctor = fhirClient
                    .read()
                    .resource(Practitioner.class)
                    .withId(appointment.getDoctorId().toString())
                    .execute();

                // Validate patient exists
                Patient patient = fhirClient
                    .read()
                    .resource(Patient.class)
                    .withId(appointment.getPatientId().toString())
                    .execute();

                // Convert entity to FHIR resource
                org.hl7.fhir.r4.model.Appointment fhirAppointment = appointmentMapper.toFhirResource(appointment);

                // Create appointment via FHIR API
                MethodOutcome outcome = fhirClient
                    .create()
                    .resource(fhirAppointment)
                    .execute();

                // Get the created resource with ID
                org.hl7.fhir.r4.model.Appointment createdAppointment =
                    (org.hl7.fhir.r4.model.Appointment) outcome.getResource();

                // Convert back to entity
                return appointmentMapper.toEntity(createdAppointment);
            } catch (ResourceNotFoundException e) {
                throw new FhirClientException("Doctor or Patient not found in FHIR server", e);
            } catch (Exception e) {
                throw new FhirClientException("Failed to create appointment in FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds an appointment by ID from the FHIR server.
     *
     * @param id Appointment ID
     * @return Mono containing the appointment, or empty if not found
     */
    public Mono<Appointment> findById(Long id) {
        return Mono.fromCallable(() -> {
            try {
                // Read appointment from FHIR API
                org.hl7.fhir.r4.model.Appointment fhirAppointment = fhirClient
                    .read()
                    .resource(org.hl7.fhir.r4.model.Appointment.class)
                    .withId(id.toString())
                    .execute();

                // Convert to entity
                return appointmentMapper.toEntity(fhirAppointment);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if not found
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve appointment from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(appt -> appt != null ? Mono.just(appt) : Mono.empty());
    }

    /**
     * Retrieves all appointments from the FHIR server.
     *
     * @return Flux of all appointments
     */
    public Flux<Appointment> findAll() {
        return Mono.fromCallable(() -> {
            try {
                // Search for all appointments with pagination
                Bundle bundle = fhirClient
                    .search()
                    .forResource(org.hl7.fhir.r4.model.Appointment.class)
                    .count(100) // Limit to 100 results
                    .returnBundle(Bundle.class)
                    .execute();

                // Extract appointment resources from bundle
                List<Appointment> appointments = bundle.getEntry().stream()
                    .filter(entry -> entry.getResource() instanceof org.hl7.fhir.r4.model.Appointment)
                    .map(entry -> (org.hl7.fhir.r4.model.Appointment) entry.getResource())
                    .map(appointmentMapper::toEntity)
                    .collect(Collectors.toList());

                return appointments;
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve appointments from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Updates an existing appointment in the FHIR server.
     * Validates that both doctor and patient exist before update.
     *
     * @param id Appointment ID
     * @param appointment Updated appointment data
     * @return Mono containing the updated appointment
     */
    public Mono<Appointment> update(Long id, Appointment appointment) {
        return Mono.fromCallable(() -> {
            try {
                // First, check if appointment exists
                org.hl7.fhir.r4.model.Appointment existingFhirAppointment = fhirClient
                    .read()
                    .resource(org.hl7.fhir.r4.model.Appointment.class)
                    .withId(id.toString())
                    .execute();

                // Validate doctor exists
                Practitioner doctor = fhirClient
                    .read()
                    .resource(Practitioner.class)
                    .withId(appointment.getDoctorId().toString())
                    .execute();

                // Validate patient exists
                Patient patient = fhirClient
                    .read()
                    .resource(Patient.class)
                    .withId(appointment.getPatientId().toString())
                    .execute();

                // Convert updated entity to FHIR resource
                org.hl7.fhir.r4.model.Appointment updatedFhirAppointment = appointmentMapper.toFhirResource(appointment);
                updatedFhirAppointment.setId(id.toString());

                // Update via FHIR API
                MethodOutcome outcome = fhirClient
                    .update()
                    .resource(updatedFhirAppointment)
                    .execute();

                // Get the updated resource
                org.hl7.fhir.r4.model.Appointment resultAppointment =
                    (org.hl7.fhir.r4.model.Appointment) outcome.getResource();

                // Convert back to entity
                return appointmentMapper.toEntity(resultAppointment);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if appointment, doctor, or patient not found
            } catch (Exception e) {
                throw new FhirClientException("Failed to update appointment in FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(updatedAppt -> updatedAppt != null ? Mono.just(updatedAppt) : Mono.empty());
    }

    /**
     * Deletes an appointment from the FHIR server.
     *
     * @param id Appointment ID
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
            try {
                // Delete appointment via FHIR API
                fhirClient
                    .delete()
                    .resourceById("Appointment", id.toString())
                    .execute();
            } catch (ResourceNotFoundException e) {
                // Ignore if already deleted
            } catch (Exception e) {
                throw new FhirClientException("Failed to delete appointment from FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
