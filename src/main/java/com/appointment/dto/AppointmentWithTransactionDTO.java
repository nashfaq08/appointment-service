package com.appointment.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentWithTransactionDTO {
    private UUID appointmentId;
    private UUID customerId;
    private String customerName;
    private UUID lawyerId;
    private String lawyerName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String description;
    private String appointmentType;
    private LocalDateTime createdAt;
    private UUID transactionId;
    private Long amountTotal;
    private Long amountSubtotal;
    private String currency;
    private String paymentStatus;
    private String paymentTransactionId;
    private String paymentMethod;
    private boolean liveMode;
    private String stripeMode;
    private LocalDateTime transactionCreatedAt;
    private String customerDetails;
}
