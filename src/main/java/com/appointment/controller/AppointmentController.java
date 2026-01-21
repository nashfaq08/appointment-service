package com.appointment.controller;

import com.appointment.dto.AppointmentDTO;
import com.appointment.dto.AppointmentOpenRequestDTO;
import com.appointment.dto.AppointmentWithTransactionDTO;
import com.appointment.dto.StripeDTO;
import com.appointment.dto.response.ApiResponse;
import com.appointment.dto.response.AvailableLawyersResponse;
import com.appointment.entities.Appointment;
import com.appointment.service.AppointmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    @PostMapping("/bookv1")
    public ResponseEntity<Appointment> bookAppointmentv1(
            @RequestBody AppointmentDTO appointmentDTO,
            Authentication authentication
    ) {
        String customerId = authentication.getName();
        return ResponseEntity.ok(appointmentService.bookAppointment(customerId, appointmentDTO));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/book")
    public ResponseEntity<Appointment> bookAppointment(
            @RequestBody StripeDTO stripeDTO,
            Authentication authentication
    ) throws JsonProcessingException {
        String customerAuthId = authentication.getName();
        return ResponseEntity.ok(appointmentService.bookAppointment(customerAuthId, stripeDTO));
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

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @PostMapping("/getAvailableLawyers")
    public ResponseEntity<List<AvailableLawyersResponse>> getAvailableLawyersForOpenAppointment(
            @RequestBody AppointmentOpenRequestDTO appointmentOpenRequestDTO,
            Authentication authentication
    ) {
        String authUserId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(appointmentService.getAvailableLawyersForOpenAppointment(authUserId, appointmentOpenRequestDTO));
    }

    @PatchMapping("/{appointmentId}/{customerAuthId}/accept")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<?> acceptAppointment(
            @PathVariable UUID appointmentId,
            @PathVariable UUID customerAuthId,
            Authentication authentication
    ) {
        String lawyerAuthUserId = authentication.getName();
        appointmentService.acceptOpenAppointment(appointmentId, lawyerAuthUserId, String.valueOf(customerAuthId));
        return ResponseEntity.ok(new ApiResponse(true, "Appointment is accepted by the Lawyer."));
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getLawyers")
    public ResponseEntity<List<Appointment>> getLawyers(
            Authentication authentication
    ) {
        String lawyerAuthUserId = authentication.getName();
        return ResponseEntity.ok(appointmentService.getByLawyer(lawyerAuthUserId));
    }

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @GetMapping("/transactions")
    public ResponseEntity<List<AppointmentWithTransactionDTO>> getAppointmentsWithTransactionsByCustomer(
            Authentication authentication
    ) {
        String customerAuthUserId = authentication.getName();
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(appointmentService.getAppointmentsWithTransactionsByCustomerId(UUID.fromString(customerAuthUserId), hasAdminRole));
    }

    @PreAuthorize("hasRole('LAWYER') or hasRole('ADMIN')")
    @GetMapping("/lawyer/transactions")
    public ResponseEntity<List<AppointmentWithTransactionDTO>> getAppointmentsWithTransactionsByLawyer(
            Authentication authentication
    ) {
        String lawyerAuthUserId = authentication.getName();
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(appointmentService.getAppointmentsWithTransactionsByLawyerId(UUID.fromString(lawyerAuthUserId), hasAdminRole));
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
