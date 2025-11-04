package com.appointment.entities;

import com.appointment.config.JsonConverter;
import com.appointment.dto.StripeDTO;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

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
    @Convert(converter = JsonConverter.class)
    @org.hibernate.annotations.ColumnTransformer(write = "?::jsonb")
    private JsonNode customerDetails;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}