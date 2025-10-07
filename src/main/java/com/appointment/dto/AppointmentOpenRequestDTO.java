package com.appointment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Data
public class AppointmentOpenRequestDTO {

    private String appointmentType;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;

}
