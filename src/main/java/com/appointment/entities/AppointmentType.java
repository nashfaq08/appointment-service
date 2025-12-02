package com.appointment.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentType {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

}
