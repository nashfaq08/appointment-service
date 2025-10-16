package com.appointment.controller;

import com.appointment.entities.Appointment;
import com.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasAuthority('INTERNAL_SERVICE')")
    @GetMapping("/appointments/lawyer/{lawyerId}")
    public ResponseEntity<List<Appointment>> byLawyer(
            @PathVariable UUID lawyerId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByLawyerId(lawyerId));
    }
}
