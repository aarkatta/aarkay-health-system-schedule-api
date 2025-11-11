package com.aarkay.health.service.impl;

import com.aarkay.health.fhir.client.DoctorFhirClient;
import com.aarkay.health.model.Doctor;
import com.aarkay.health.service.DoctorService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorFhirClient doctorFhirClient;

    public DoctorServiceImpl(DoctorFhirClient doctorFhirClient) {
        this.doctorFhirClient = doctorFhirClient;
    }

    @Override
    public Flux<Doctor> getAllDoctors() {
        return doctorFhirClient.findAll();
    }

    @Override
    public Mono<Doctor> getDoctorById(Long id) {
        return doctorFhirClient.findById(id);
    }

    @Override
    public Mono<Doctor> createDoctor(Doctor doctor) {
        return doctorFhirClient.create(doctor);
    }

    @Override
    public Mono<Doctor> updateDoctor(Long id, Doctor doctor) {
        return doctorFhirClient.findById(id)
                .flatMap(existingDoctor -> {
                    existingDoctor.setFirstName(doctor.getFirstName());
                    existingDoctor.setLastName(doctor.getLastName());
                    existingDoctor.setSpecialty(doctor.getSpecialty());
                    existingDoctor.setZipcode(doctor.getZipcode());
                    existingDoctor.setPhone(doctor.getPhone());
                    existingDoctor.setEmail(doctor.getEmail());
                    return doctorFhirClient.update(id, existingDoctor);
                });
    }

    @Override
    public Mono<Void> deleteDoctor(Long id) {
        return doctorFhirClient.delete(id);
    }

    @Override
    public Flux<Doctor> getDoctorsByZipcodeAndSpecialty(String zipcode, String specialty) {
        return doctorFhirClient.searchByZipcodeAndSpecialty(zipcode, specialty);
    }
}
