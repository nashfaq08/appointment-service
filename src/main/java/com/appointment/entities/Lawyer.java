package com.appointment.entities;

import com.appointment.constants.LawyerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Lawyer {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String barId;

    private String name;

    private String email;

    private String password;

    private int yearsOfExp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LawyerStatus status = LawyerStatus.INCOMPLETE;

    @ManyToMany
    @JoinTable(
            name = "lawyer_services",
            joinColumns = @JoinColumn(name = "lawyer_id"),
            inverseJoinColumns = @JoinColumn(name = "srv_id")
    )
    private List<Services> services;

}

