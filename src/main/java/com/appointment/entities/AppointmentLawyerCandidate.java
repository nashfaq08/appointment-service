package com.appointment.entities;

import com.appointment.constants.CandidateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_lawyer_candidate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"appointment_id", "lawyer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentLawyerCandidate {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "lawyer_id", nullable = false)
    private UUID lawyerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateStatus status;

    @CreationTimestamp
    private LocalDateTime notifiedAt;
}

