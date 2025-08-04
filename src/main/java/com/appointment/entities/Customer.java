package com.appointment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // aligns with DB: customer(id)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_no")
    private String contactNo;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // One customer can upload multiple documents
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    // One customer can have multiple service requests
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestHistory> requests;
}

