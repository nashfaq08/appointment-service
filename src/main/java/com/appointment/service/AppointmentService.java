package com.appointment.service;

import com.appointment.client.ProfileServiceClient;
import com.appointment.constants.AppointmentStatus;
import com.appointment.dto.AppointmentDTO;
import com.appointment.dto.AppointmentOpenRequestDTO;
import com.appointment.dto.AvailabilityCheckRequestDTO;
import com.appointment.dto.OpenAppointmentSearchDTO;
import com.appointment.entities.Appointment;
import com.appointment.entities.AppointmentType;
import com.appointment.exception.ApiException;
import com.appointment.repositories.AppointmentRepository;
import com.appointment.repositories.AppointmentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final ProfileServiceClient profileServiceClient;

    @Transactional
    public Appointment bookAppointment(String customerId, AppointmentDTO appointmentDTO) {

        // Step 1: Customer existence check
        if (!profileServiceClient.isCustomerExist(UUID.fromString(customerId))) {
            throw new ApiException("Invalid or unapproved customer.", "INVALID_CUSTOMER_ID", HttpStatus.BAD_REQUEST);
        }

        // Step 2: Check availability via Profile service
        AvailabilityCheckRequestDTO availabilityCheckRequestDTO = AvailabilityCheckRequestDTO.builder()
                .appointmentDate(appointmentDTO.getAppointmentDate())
                .lawyerId(appointmentDTO.getLawyerId())
                .startTime(appointmentDTO.getStartTime())
                .endTime(appointmentDTO.getStartTime().plusMinutes(30))
                .build();

        boolean isAppointmentAvailable = profileServiceClient.checkAppointmentAvailability(availabilityCheckRequestDTO);

        if (!isAppointmentAvailable) {
            throw new ApiException("The selected time slot is not available for the specified lawyer.", "TIME_SLOT_NOT_AVAILABLE", HttpStatus.NOT_FOUND);
        }

        // Step 3: Check if appointment overlaps with existing ones on the same weekday
        DayOfWeek requestedDayOfWeek = appointmentDTO.getAppointmentDate().getDayOfWeek();

        List<Appointment> existingAppointments = appointmentRepository
                .findByLawyerIdAndDayOfWeek(appointmentDTO.getLawyerId(), requestedDayOfWeek.getValue());

        for (Appointment existing : existingAppointments) {
            boolean overlaps = !(appointmentDTO.getStartTime().plusMinutes(30).isBefore(existing.getStartTime())
                    || appointmentDTO.getStartTime().isAfter(existing.getEndTime()));
            if (overlaps) {
                throw new ApiException("An appointment already exists for the specified time slot.", "APPOINTMENT_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
            }
        }

        // Step 4: Prepare and save
        UUID custId = UUID.fromString(customerId);

        AppointmentType appointmentType = appointmentTypeRepository.findByName(appointmentDTO.getAppointmentType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment type"));

        Appointment appointment = Appointment.builder()
                .customerId(custId)
                .lawyerId(appointmentDTO.getLawyerId())
                .appointmentDate(appointmentDTO.getAppointmentDate())
                .startTime(appointmentDTO.getStartTime())
                .endTime(appointmentDTO.getStartTime().plusMinutes(30))
                .description(appointmentDTO.getDescription())
                .status(AppointmentStatus.BOOKED)
                .appointmentType(appointmentType)
                .build();

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment createOpenAppointment(String customerId, AppointmentOpenRequestDTO appointmentOpenRequestDTO) {

        // Step 1: Customer existence check
        if (!profileServiceClient.isCustomerExist(UUID.fromString(customerId))) {
            throw new ApiException("Invalid or unapproved customer.", "INVALID_CUSTOMER_ID", HttpStatus.BAD_REQUEST);
        }

        // Step 2: Prepare request for profile service
        OpenAppointmentSearchDTO openAppointmentSearchDTO = OpenAppointmentSearchDTO.builder()
                        .appointmentType(appointmentOpenRequestDTO.getAppointmentType())
                        .appointmentDate(appointmentOpenRequestDTO.getAppointmentDate())
                        .startTime(appointmentOpenRequestDTO.getStartTime())
                        .endTime(appointmentOpenRequestDTO.getEndTime())
                        .build();

        // Step 3: Call profile service to get available lawyers
        List<String> availableLawyers = profileServiceClient.getAvailableLawyers(openAppointmentSearchDTO);

        if (availableLawyers == null || availableLawyers.isEmpty()) {
            throw new ApiException("No available lawyers found for the requested time slot and apppointment type.", "NO_LAWYERS_FOUND",  HttpStatus.NOT_FOUND);
        }

        // Step 4: Have a list of available lawyers and will need to send the notification to them and based on that the logic will handle the acceptance of the lawyer who selected the appointment
        // Select the first available lawyer (you can improve this logic later)
        UUID selectedLawyerId = UUID.fromString(String.valueOf(availableLawyers.get(0)));

        AppointmentType type = appointmentTypeRepository.findByName(appointmentOpenRequestDTO.getAppointmentType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment type"));

        // Step 5: Save the appointment
        Appointment appointment = Appointment.builder()
                .customerId(UUID.fromString(customerId))
                .lawyerId(selectedLawyerId)
                .appointmentType(type)
                .appointmentDate(appointmentOpenRequestDTO.getAppointmentDate())
                .startTime(appointmentOpenRequestDTO.getStartTime())
                .endTime(appointmentOpenRequestDTO.getEndTime())
                .description(appointmentOpenRequestDTO.getDescription())
                .status(AppointmentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public List<String> getAvailableLawyersForOpenAppointment(String authUserId, AppointmentOpenRequestDTO appointmentOpenRequestDTO) {

        // Step 1: Customer existence check
        if (!profileServiceClient.isCustomerExist(UUID.fromString(authUserId))) {
            throw new ApiException("Invalid or Unapproved customer.", "INVALID_CUSTOMER_AUTH_ID", HttpStatus.BAD_REQUEST);
        }

        // Step 2: Prepare request for profile service
        OpenAppointmentSearchDTO openAppointmentSearchDTO = OpenAppointmentSearchDTO.builder()
                .appointmentType(appointmentOpenRequestDTO.getAppointmentType())
                .appointmentDate(appointmentOpenRequestDTO.getAppointmentDate())
                .startTime(appointmentOpenRequestDTO.getStartTime())
                .endTime(appointmentOpenRequestDTO.getEndTime())
                .build();

        // Step 3: Call profile service to get available lawyers
        List<String> availableLawyers = profileServiceClient.getAvailableLawyers(openAppointmentSearchDTO);

        if (availableLawyers == null || availableLawyers.isEmpty()) {
            throw new ApiException("No available lawyers found for the requested time slot and apppointment type.", "NO_LAWYERS_FOUND", HttpStatus.NOT_FOUND);
        }

        return availableLawyers;
    }

    @Transactional
    public void acceptOpenAppointment(UUID appointmentId, String lawyerAuthUserId) {

        // Verify lawyer existence & status (via profile service)
        if (!profileServiceClient.isLawyerValid(UUID.fromString(lawyerAuthUserId))) {
            throw new ApiException("Lawyer not authorized to accept this appointment.", "LAWYER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.OPEN) {
            throw new ApiException("Appointment already taken.", "APPOINTMENT_ALREADY_BOOKED", HttpStatus.BAD_REQUEST);
        }

        // Assign appointment to lawyer
        appointment.setLawyerId(UUID.fromString(lawyerAuthUserId));
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointmentRepository.save(appointment);
    }

    public List<Appointment> getByCustomer(String customerAuthUserId) {
        if (!profileServiceClient.isCustomerExist(UUID.fromString(customerAuthUserId))) {
            throw new ApiException("Customer not authorized to list the appointments.", "CUSTOMER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return appointmentRepository.findAllByCustomerId(UUID.fromString(customerAuthUserId));
    }

    public List<Appointment> getByLawyer(String lawyerAuthUserId) {
        if (!profileServiceClient.isLawyerValid(UUID.fromString(lawyerAuthUserId))) {
            throw new ApiException("lawyer not authorized to list the appointments.", "LAWYER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return appointmentRepository.findAllByLawyerId(UUID.fromString(lawyerAuthUserId));
    }

    public List<Appointment> getAppointmentsByLawyerId(UUID lawyerId) {
        if (!profileServiceClient.isLawyerValid(lawyerId)) {
            throw new ApiException("lawyer not authorized to list the appointments.", "LAWYER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return appointmentRepository.findAllByLawyerId(lawyerId);
    }

//    public List<Appointment> getByLawyer(UUID lawyerId) {
//        return appointmentRepository.findByLawyerId(lawyerId);
//    }
//
//    public void updateStatus(Long id, AppointmentStatus status) {
//        Appointment appointment = appointmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Appointment not found"));
//        appointment.setStatus(status);
//        appointmentRepository.save(appointment);
//    }
//
//    public void cancel(Long id) {
//        appointmentRepository.deleteById(id);
//    }

}
