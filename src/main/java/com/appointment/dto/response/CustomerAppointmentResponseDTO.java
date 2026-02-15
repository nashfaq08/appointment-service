package com.appointment.dto.response;

import com.appointment.constants.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class CustomerAppointmentResponseDTO {
    private UUID appointmentId;
    private AppointmentStatus status;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID lawyerId;            // only if assigned
    private String lawyerName;        // optional (fetched from profile service)
    private int invitedLawyersCount;  // for open request
    private int acceptedCount;
}
