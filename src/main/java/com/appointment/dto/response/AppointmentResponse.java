package com.appointment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Data
public class AppointmentResponse {
    private Long id;

    private UUID customerId;
    private UUID lawyerId;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String status;
    private String description;

    private String appointmentType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

