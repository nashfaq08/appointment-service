package com.appointment.entities;

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
public class LawCategory {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "law_category_services",
            joinColumns = @JoinColumn(name = "lc_id"),
            inverseJoinColumns = @JoinColumn(name = "srv_id")
    )
    private List<Services> services;
}

