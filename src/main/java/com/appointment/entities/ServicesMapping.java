package com.appointment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ServicesMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "srv_map_id")
    private Long srvMapId;

    @ManyToOne
    @JoinColumn(name = "lawyer_id", nullable = false)
    private LawyerProfile lawyerProfile;

    @ManyToOne
    @JoinColumn(name = "srv_id", nullable = false)
    private Services service;

    // Optional metadata
    // private LocalDateTime mappedAt = LocalDateTime.now();
}
