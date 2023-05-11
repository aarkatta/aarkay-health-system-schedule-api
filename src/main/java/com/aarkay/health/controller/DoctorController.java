package com.aarkay.health.controller;

import com.aarkay.health.model.Doctor;
import com.aarkay.health.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public  Flux<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Doctor>> getDoctorById(@PathVariable("id") Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(doctor))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Flux<Doctor> getDoctorByZipCodeAndSpecialty(@RequestParam String zipcode, @RequestParam String specialty) {
        return doctorService.getDoctorsByZipcodeAndSpecialty(zipcode, specialty);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return doctorService.createDoctor(doctor);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Doctor>> updateDoctor(@PathVariable("id") Long id, @RequestBody Doctor doctor) {
        return doctorService.updateDoctor(id, doctor)
                .map(updatedDoctor -> ResponseEntity.ok(updatedDoctor))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDoctor(@PathVariable("id") Long id) {
        return doctorService.deleteDoctor(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
