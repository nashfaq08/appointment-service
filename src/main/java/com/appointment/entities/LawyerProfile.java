package com.appointment.entities;

import com.appointment.constants.LawyerStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private UUID authUserId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "bar_id_number", nullable = false)
    private String barIdNumber;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LawyerStatus status = LawyerStatus.PENDING_APPROVAL;

    @ManyToOne
    @JoinColumn(name = "speciality_id")
    private Speciality speciality;

    @OneToMany(mappedBy = "lawyerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    @OneToMany(mappedBy = "lawyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    @Column(name = "location")
    private String location;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @ManyToMany
    @JoinTable(
            name = "lawyer_services",
            joinColumns = @JoinColumn(name = "lawyer_id"),
            inverseJoinColumns = @JoinColumn(name = "srv_id")
    )
    private List<Services> services;

    // Uncomment if you plan to re-enable AvailabilityMode enum
    /*
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_mode")
    private AvailabilityMode availabilityMode; // REMOTE, IN_PERSON, BOTH
    */

//    @ElementCollection
//    @CollectionTable(name = "lawyer_languages", joinColumns = @JoinColumn(name = "lawyer_id"))
//    @Column(name = "language")
//    private List<String> languageSpoken;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;

    @Column(name = "verified")
    private boolean verified = false;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}