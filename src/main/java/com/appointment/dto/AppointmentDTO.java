package com.appointment.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class AppointmentDTO {
    private UUID lawyerId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String description;
    private String appointmentType;
    private DayOfWeek day;
}
