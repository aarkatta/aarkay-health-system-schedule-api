package com.aarkay.health.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "patient")
public class Patient {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    private String gender;
    private String phone;
}


