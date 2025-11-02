package com.appointment.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amountSubtotal;
    private Long amountTotal;
    private String currency;
    private String paymentStatus;
    private String stripeStatus;
    private String paymentTransactionId;
    private String paymentMethodType;
    private boolean livemode;
    private String mode;

    @Column(columnDefinition = "jsonb")
    private String customerDetails;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}

