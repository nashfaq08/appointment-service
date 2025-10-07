package com.appointment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
public class OpenAppointmentSearchDTO {

    private String appointmentType;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

}
