package com.aarkay.health.fhir.client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.aarkay.health.exception.FhirClientException;
import com.aarkay.health.mapper.PatientMapper;
import com.aarkay.health.model.Patient;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FHIR client for Patient resource operations.
 * Provides reactive methods to interact with FHIR Patient API.
 */
@Component
public class PatientFhirClient {

    private final IGenericClient fhirClient;
    private final PatientMapper patientMapper;

    public PatientFhirClient(IGenericClient fhirClient, PatientMapper patientMapper) {
        this.fhirClient = fhirClient;
        this.patientMapper = patientMapper;
    }

    /**
     * Creates a new patient in the FHIR server.
     *
     * @param patient Patient entity to create
     * @return Mono containing the created patient with ID
     */
    public Mono<Patient> create(Patient patient) {
        return Mono.fromCallable(() -> {
            try {
                // Convert entity to FHIR resource
                org.hl7.fhir.r4.model.Patient fhirPatient = patientMapper.toFhirResource(patient);

                // Create patient via FHIR API
                MethodOutcome outcome = fhirClient
                    .create()
                    .resource(fhirPatient)
                    .execute();

                // Get the created resource with ID
                org.hl7.fhir.r4.model.Patient createdPatient =
                    (org.hl7.fhir.r4.model.Patient) outcome.getResource();

                // Convert back to entity
                return patientMapper.toEntity(createdPatient);
            } catch (Exception e) {
                throw new FhirClientException("Failed to create patient in FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds a patient by ID from the FHIR server.
     *
     * @param id Patient ID
     * @return Mono containing the patient, or empty if not found
     */
    public Mono<Patient> findById(Long id) {
        return Mono.fromCallable(() -> {
            try {
                // Read patient from FHIR API
                org.hl7.fhir.r4.model.Patient fhirPatient = fhirClient
                    .read()
                    .resource(org.hl7.fhir.r4.model.Patient.class)
                    .withId(id.toString())
                    .execute();

                // Convert to entity
                return patientMapper.toEntity(fhirPatient);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if not found, will be handled as empty Mono
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve patient from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(patient -> patient != null ? Mono.just(patient) : Mono.empty());
    }

    /**
     * Retrieves all patients from the FHIR server.
     *
     * @return Flux of all patients
     */
    public Flux<Patient> findAll() {
        return Mono.fromCallable(() -> {
            try {
                // Search for all patients with pagination
                Bundle bundle = fhirClient
                    .search()
                    .forResource(org.hl7.fhir.r4.model.Patient.class)
                    .count(100) // Limit to 100 results
                    .returnBundle(Bundle.class)
                    .execute();

                // Extract patient resources from bundle
                List<Patient> patients = bundle.getEntry().stream()
                    .filter(entry -> entry.getResource() instanceof org.hl7.fhir.r4.model.Patient)
                    .map(entry -> (org.hl7.fhir.r4.model.Patient) entry.getResource())
                    .map(patientMapper::toEntity)
                    .collect(Collectors.toList());

                return patients;
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve patients from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Updates an existing patient in the FHIR server.
     *
     * @param id Patient ID
     * @param patient Updated patient data
     * @return Mono containing the updated patient
     */
    public Mono<Patient> update(Long id, Patient patient) {
        return Mono.fromCallable(() -> {
            try {
                // First, check if patient exists
                org.hl7.fhir.r4.model.Patient existingFhirPatient = fhirClient
                    .read()
                    .resource(org.hl7.fhir.r4.model.Patient.class)
                    .withId(id.toString())
                    .execute();

                // Convert updated entity to FHIR resource
                org.hl7.fhir.r4.model.Patient updatedFhirPatient = patientMapper.toFhirResource(patient);
                updatedFhirPatient.setId(id.toString());

                // Update via FHIR API
                MethodOutcome outcome = fhirClient
                    .update()
                    .resource(updatedFhirPatient)
                    .execute();

                // Get the updated resource
                org.hl7.fhir.r4.model.Patient resultPatient =
                    (org.hl7.fhir.r4.model.Patient) outcome.getResource();

                // Convert back to entity
                return patientMapper.toEntity(resultPatient);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if not found
            } catch (Exception e) {
                throw new FhirClientException("Failed to update patient in FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(updatedPatient -> updatedPatient != null ? Mono.just(updatedPatient) : Mono.empty());
    }

    /**
     * Deletes a patient from the FHIR server.
     *
     * @param id Patient ID
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
            try {
                // Delete patient via FHIR API
                fhirClient
                    .delete()
                    .resourceById("Patient", id.toString())
                    .execute();
            } catch (ResourceNotFoundException e) {
                // Ignore if already deleted
            } catch (Exception e) {
                throw new FhirClientException("Failed to delete patient from FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
