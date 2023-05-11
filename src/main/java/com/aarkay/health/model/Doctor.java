package com.aarkay.health.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "doctor")
public class Doctor {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
    private String zipcode;
    private String email;
}
