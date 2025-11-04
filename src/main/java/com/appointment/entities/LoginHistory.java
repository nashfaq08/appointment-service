package com.appointment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LoginHistory {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String credentials;

    @Column(nullable = false)
    private String status;

    // Optional: to track from where login was attempted
    // @Column(name = "ip_address")
    // private String ipAddress;
}
