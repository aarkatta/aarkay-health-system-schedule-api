package com.aarkay.health.service.impl;

import com.aarkay.health.model.Doctor;
import com.aarkay.health.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void getAllDoctors() {
        when(doctorRepository.findAll()).thenReturn(Flux.just(
                new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "abc@abcdoctor.com"),
                new Doctor(2L, "Jane", "Doe", "Orthopedics", "212-212-2255", "12345", "def@defdoctor.com"),
                new Doctor(3L, "John", "Smith", "Internal Medicine", "212-212-5522", "12345", "ghi@ghidoctor.com")
        ));

        Flux<Doctor> doctors = doctorService.getAllDoctors();
        assertNotNull(doctors);
        assertEquals(3, doctors.count().block());
    }

    @Test
    void getDoctorById() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "doctorabc@abcemail.com");
        when(doctorRepository.findById(1L)).thenReturn(Mono.just(doctor));
        Mono<Doctor> doctorMono = doctorService.getDoctorById(1L);
        assertNotNull(doctorMono);
        assertEquals("John", doctorMono.block().getFirstName());
        assertEquals("Doe", doctorMono.block().getLastName());
        assertEquals("Cardiology", doctorMono.block().getSpecialty());
        assertEquals("212-212-2121", doctorMono.block().getPhone());
    }

    @Test
    void createDoctor() {
        Doctor doctor = new Doctor(4L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        when(doctorRepository.save(doctor)).thenReturn(Mono.just(doctor));
        Mono<Doctor> doctorMono = doctorService.createDoctor(doctor);
        assertNotNull(doctorMono);
        assertEquals("John", doctorMono.block().getFirstName());
        assertEquals("Doe", doctorMono.block().getLastName());
        assertEquals("Cardiology", doctorMono.block().getSpecialty());
        assertEquals("212-212-2121", doctorMono.block().getPhone());
        assertEquals(4L, doctorMono.block().getId());
    }

    @Test
    void updateDoctor() {
        Doctor doctor = new Doctor(4L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        when(doctorRepository.findById(4L)).thenReturn(Mono.just(doctor));
        when(doctorRepository.save(doctor)).thenReturn(Mono.just(doctor));
        Mono<Doctor> doctorMono = doctorService.updateDoctor(4L, doctor);
        assertNotNull(doctorMono);
        assertEquals("John", doctorMono.block().getFirstName());
        assertEquals("Doe", doctorMono.block().getLastName());
        assertEquals("Cardiology", doctorMono.block().getSpecialty());
        assertEquals("212-212-2121", doctorMono.block().getPhone());
        assertEquals(4L, doctorMono.block().getId());

    }

    @Test
    void deleteDoctor() {
        Doctor doctor = new Doctor(4L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "jkl@jkldctor.com");
        when(doctorRepository.deleteById(doctor.getId())).thenReturn(Mono.empty());
        Mono<Void> doctorMono = doctorService.deleteDoctor(doctor.getId());
        assertNotNull(doctorMono);
        assertEquals(Mono.empty(), doctorMono);
    }

    @Test
    void getDoctorsByZipcodeAndSpecialty() {
        Doctor doctor = new Doctor(1L, "John", "Doe", "Cardiology", "212-212-2121", "12345", "doctorabc@abcemail.com");
        when(doctorRepository.findByZipcodeAndSpecialty("12345", "Cardiology")).thenReturn(Flux.just(doctor));
        Flux<Doctor> doctorFlux = doctorService.getDoctorsByZipcodeAndSpecialty("12345", "Cardiology");
        assertNotNull(doctorFlux);
        assertEquals(1, doctorFlux.count().block());
        assertEquals("John", doctorFlux.blockFirst().getFirstName());
        assertEquals("Doe", doctorFlux.blockFirst().getLastName());
        assertEquals("Cardiology", doctorFlux.blockFirst().getSpecialty());
        assertEquals("212-212-2121", doctorFlux.blockFirst().getPhone());

    }
}