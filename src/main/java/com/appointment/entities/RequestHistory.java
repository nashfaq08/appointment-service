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
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "cust_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "lawyer_id", nullable = false)
    private LawyerProfile lawyer;

    @ManyToOne
    @JoinColumn(name = "sdu_id")
    private Services service;

    @ManyToOne
    @JoinColumn(name = "spc_id")
    private Speciality speciality;

    @ManyToOne
    @JoinColumn(name = "doc_id")
    private Document document;
}

