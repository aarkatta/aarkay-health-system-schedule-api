package com.aarkay.health.repository;

import com.aarkay.health.model.Appointment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, Long> {
}
