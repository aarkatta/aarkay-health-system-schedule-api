package com.aarkay.health.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "appointment")
public class Appointment {
    @Id
    private Long id;
    private LocalDateTime appointmentTime;
    private Long doctorId;
    private Long patientId;
}
