package com.appointment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Data
public class AvailabilityCheckRequestDTO {
    private UUID lawyerId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
