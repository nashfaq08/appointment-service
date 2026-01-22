package com.appointment.controller;

import com.appointment.dto.StripeDTO;
import com.appointment.entities.Appointment;
import com.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasAuthority('INTERNAL_SERVICE')")
    @GetMapping("/pending/appointments/lawyer/{lawyerId}")
    public ResponseEntity<List<Appointment>> pendingAppointmentsByLawyer(
            @PathVariable UUID lawyerId
    ) {
        return ResponseEntity.ok(appointmentService.getPendingAppointmentsByLawyer(lawyerId));
    }

//    @PreAuthorize("hasAuthority('INTERNAL_SERVICE')")
//    @PostMapping("/book")
//    public ResponseEntity<Appointment> bookAppointmentv1(
//            @RequestBody StripeDTO stripeDTO
//    ) {
//        return ResponseEntity.ok(appointmentService.bookAppointment(stripeDTO));
//    }
}
