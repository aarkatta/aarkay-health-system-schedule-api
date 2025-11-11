package com.aarkay.health.fhir.client;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.aarkay.health.exception.FhirClientException;
import com.aarkay.health.mapper.DoctorMapper;
import com.aarkay.health.model.Doctor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FHIR client for Practitioner (Doctor) resource operations.
 * Provides reactive methods to interact with FHIR Practitioner API.
 */
@Component
public class DoctorFhirClient {

    private final IGenericClient fhirClient;
    private final DoctorMapper doctorMapper;

    public DoctorFhirClient(IGenericClient fhirClient, DoctorMapper doctorMapper) {
        this.fhirClient = fhirClient;
        this.doctorMapper = doctorMapper;
    }

    /**
     * Creates a new doctor (practitioner) in the FHIR server.
     *
     * @param doctor Doctor entity to create
     * @return Mono containing the created doctor with ID
     */
    public Mono<Doctor> create(Doctor doctor) {
        return Mono.fromCallable(() -> {
            try {
                // Convert entity to FHIR resource
                Practitioner fhirPractitioner = doctorMapper.toFhirResource(doctor);

                // Create practitioner via FHIR API
                MethodOutcome outcome = fhirClient
                    .create()
                    .resource(fhirPractitioner)
                    .execute();

                // Get the created resource with ID
                Practitioner createdPractitioner = (Practitioner) outcome.getResource();

                // Convert back to entity
                return doctorMapper.toEntity(createdPractitioner);
            } catch (Exception e) {
                throw new FhirClientException("Failed to create doctor in FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds a doctor by ID from the FHIR server.
     *
     * @param id Doctor ID
     * @return Mono containing the doctor, or empty if not found
     */
    public Mono<Doctor> findById(Long id) {
        return Mono.fromCallable(() -> {
            try {
                // Read practitioner from FHIR API
                Practitioner fhirPractitioner = fhirClient
                    .read()
                    .resource(Practitioner.class)
                    .withId(id.toString())
                    .execute();

                // Convert to entity
                return doctorMapper.toEntity(fhirPractitioner);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if not found
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve doctor from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(doctor -> doctor != null ? Mono.just(doctor) : Mono.empty());
    }

    /**
     * Retrieves all doctors from the FHIR server.
     *
     * @return Flux of all doctors
     */
    public Flux<Doctor> findAll() {
        return Mono.fromCallable(() -> {
            try {
                // Search for all practitioners with pagination
                Bundle bundle = fhirClient
                    .search()
                    .forResource(Practitioner.class)
                    .count(100) // Limit to 100 results
                    .returnBundle(Bundle.class)
                    .execute();

                // Extract practitioner resources from bundle
                List<Doctor> doctors = bundle.getEntry().stream()
                    .filter(entry -> entry.getResource() instanceof Practitioner)
                    .map(entry -> (Practitioner) entry.getResource())
                    .map(doctorMapper::toEntity)
                    .collect(Collectors.toList());

                return doctors;
            } catch (Exception e) {
                throw new FhirClientException("Failed to retrieve doctors from FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Searches for doctors by zipcode and specialty.
     * Uses FHIR search parameters: address-postalcode and qualification.
     *
     * @param zipcode Postal code to search
     * @param specialty Medical specialty to search
     * @return Flux of matching doctors
     */
    public Flux<Doctor> searchByZipcodeAndSpecialty(String zipcode, String specialty) {
        return Mono.fromCallable(() -> {
            try {
                // Search practitioners by address-postalcode and qualification
                Bundle bundle = fhirClient
                    .search()
                    .forResource(Practitioner.class)
                    .where(Practitioner.ADDRESS_POSTALCODE.matches().value(zipcode))
                    .and(Practitioner.QUALIFICATION.exactly().code(specialty))
                    .count(100)
                    .returnBundle(Bundle.class)
                    .execute();

                // Extract and convert practitioner resources
                List<Doctor> doctors = bundle.getEntry().stream()
                    .filter(entry -> entry.getResource() instanceof Practitioner)
                    .map(entry -> (Practitioner) entry.getResource())
                    .map(doctorMapper::toEntity)
                    .collect(Collectors.toList());

                return doctors;
            } catch (Exception e) {
                throw new FhirClientException("Failed to search doctors by zipcode and specialty", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
    }

    /**
     * Updates an existing doctor in the FHIR server.
     *
     * @param id Doctor ID
     * @param doctor Updated doctor data
     * @return Mono containing the updated doctor
     */
    public Mono<Doctor> update(Long id, Doctor doctor) {
        return Mono.fromCallable(() -> {
            try {
                // First, check if practitioner exists
                Practitioner existingFhirPractitioner = fhirClient
                    .read()
                    .resource(Practitioner.class)
                    .withId(id.toString())
                    .execute();

                // Convert updated entity to FHIR resource
                Practitioner updatedFhirPractitioner = doctorMapper.toFhirResource(doctor);
                updatedFhirPractitioner.setId(id.toString());

                // Update via FHIR API
                MethodOutcome outcome = fhirClient
                    .update()
                    .resource(updatedFhirPractitioner)
                    .execute();

                // Get the updated resource
                Practitioner resultPractitioner = (Practitioner) outcome.getResource();

                // Convert back to entity
                return doctorMapper.toEntity(resultPractitioner);
            } catch (ResourceNotFoundException e) {
                return null; // Return null if not found
            } catch (Exception e) {
                throw new FhirClientException("Failed to update doctor in FHIR server", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(updatedDoctor -> updatedDoctor != null ? Mono.just(updatedDoctor) : Mono.empty());
    }

    /**
     * Deletes a doctor from the FHIR server.
     *
     * @param id Doctor ID
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
            try {
                // Delete practitioner via FHIR API
                fhirClient
                    .delete()
                    .resourceById("Practitioner", id.toString())
                    .execute();
            } catch (ResourceNotFoundException e) {
                // Ignore if already deleted
            } catch (Exception e) {
                throw new FhirClientException("Failed to delete doctor from FHIR server", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
