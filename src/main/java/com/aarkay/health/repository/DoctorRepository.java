package com.aarkay.health.repository;

import com.aarkay.health.model.Doctor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DoctorRepository extends ReactiveCrudRepository<Doctor, Long> {
    Flux<Doctor> findByZipcodeAndSpecialty(String zipcode, String specialty);
}
