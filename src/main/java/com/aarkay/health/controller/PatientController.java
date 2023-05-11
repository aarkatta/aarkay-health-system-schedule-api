package com.aarkay.health.controller;

import com.aarkay.health.model.Patient;
import com.aarkay.health.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Flux<Patient> getAllPatients() {
        return  patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Patient>> getPatientById(@PathVariable("id") Long id) {
        return patientService.getPatientById(id)
                .map(patient -> ResponseEntity.ok(patient))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Patient> createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Patient>> updatePatient(@PathVariable("id") Long id, @RequestBody Patient patient) {
        return patientService.updatePatient(id, patient)
                .map(updatedPatient -> ResponseEntity.ok(updatedPatient))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePatient(@PathVariable("id") Long id) {
        return patientService.deletePatient(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
