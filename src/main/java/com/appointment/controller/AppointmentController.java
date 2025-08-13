package com.appointment.controller;

import com.appointment.dto.AppointmentDTO;
import com.appointment.dto.AppointmentOpenRequestDTO;
import com.appointment.dto.response.ApiResponse;
import com.appointment.entities.Appointment;
import com.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/book")
    public ResponseEntity<Appointment> bookAppointment(
            @RequestBody AppointmentDTO appointmentDTO,
            Authentication authentication
    ) {
        String customerId = authentication.getName();
        return ResponseEntity.ok(appointmentService.bookAppointment(customerId, appointmentDTO));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/book/openAppointment")
    public ResponseEntity<?> createOpenAppointment(
            @RequestBody AppointmentOpenRequestDTO appointmentOpenRequestDTO,
            Authentication authentication
    ) {
        String customerId = authentication.getName();
        Appointment appointment = appointmentService.createOpenAppointment(customerId, appointmentOpenRequestDTO);
        return ResponseEntity.ok(appointment);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/getAvailableLawyers")
    public ResponseEntity<List<String>> getAvailableLawyersForOpenAppointment(
            @RequestBody AppointmentOpenRequestDTO appointmentOpenRequestDTO,
            Authentication authentication
    ) {
        String customerId = authentication.getName();
        return ResponseEntity.ok(appointmentService.getAvailableLawyersForOpenAppointment(customerId, appointmentOpenRequestDTO));
    }

    @PatchMapping("/{appointmentId}/accept")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<?> acceptAppointment(
            @PathVariable UUID appointmentId,
            Authentication authentication
    ) {
        String lawyerAuthUserId = authentication.getName();
        appointmentService.acceptOpenAppointment(appointmentId, lawyerAuthUserId);
        return ResponseEntity.ok(new ApiResponse(true, "Appointment accepted."));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<List<Appointment>> byCustomer(
            Authentication authentication
    ) {
        String customerAuthUserId = authentication.getName();
        return ResponseEntity.ok(appointmentService.getByCustomer(customerAuthUserId));
    }

    @PreAuthorize("hasRole('LAWYER')")
    @GetMapping("/lawyer")
    public ResponseEntity<List<Appointment>> byLawyer(
            Authentication authentication
    ) {
        String lawyerAuthUserId = authentication.getName();
        return ResponseEntity.ok(appointmentService.getByLawyer(lawyerAuthUserId));
    }

//    @GetMapping("/lawyer/{lawyerId}")
//    public ResponseEntity<List<Appointment>> byLawyer(@PathVariable UUID lawyerId) {
//        return ResponseEntity.ok(appointmentService.getByLawyer(lawyerId));
//    }
//
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
//        appointmentService.updateStatus(id, status);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> cancel(@PathVariable Long id) {
//        appointmentService.cancel(id);
//        return ResponseEntity.noContent().build();
//    }

}
