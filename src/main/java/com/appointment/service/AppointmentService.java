package com.appointment.service;

import com.appointment.client.AuthServiceClient;
import com.appointment.client.ProfileServiceClient;
import com.appointment.constants.AppointmentStatus;
import com.appointment.dto.*;
import com.appointment.entities.Appointment;
import com.appointment.entities.AppointmentType;
import com.appointment.entities.Transaction;
import com.appointment.exception.ApiException;
import com.appointment.repositories.AppointmentRepository;
import com.appointment.repositories.AppointmentTypeRepository;
import com.appointment.repositories.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final TransactionRepository transactionRepository;
    private final ProfileServiceClient profileServiceClient;
    private final AuthServiceClient authServiceClient;

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
                .findByLawyerIdAndAppointmentDate(appointmentDTO.getLawyerId(), appointmentDTO.getAppointmentDate());

        checkAppointmentOverlap(appointmentDTO.getStartTime(), existingAppointments);

        // Step 4: Prepare and save
        UUID custId = UUID.fromString(customerId);

        AppointmentType appointmentType = appointmentTypeRepository.findByNameIgnoreCase(appointmentDTO.getAppointmentType())
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
    public void checkAppointmentOverlap(LocalTime startTime, List<Appointment> existingAppointments) {

        LocalTime newStart = startTime;
        LocalTime newEnd = newStart.plusMinutes(30);  // 30-minute slot

        for (Appointment existing : existingAppointments) {

            LocalTime existingStart = existing.getStartTime();
            LocalTime existingEnd = existingStart.plusMinutes(30); // fixed 30-minute slot

            boolean overlaps = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);

            if (overlaps) {
                throw new ApiException(
                        "An appointment already exists for the specified time slot.",
                        "APPOINTMENT_ALREADY_EXISTS",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    @Transactional
    public Appointment bookAppointment(String customerAuthId, StripeDTO stripeDTO) throws JsonProcessingException {

        log.info("Starting to book the appointment after payment has been successfully charged {}", new ObjectMapper().writeValueAsString(stripeDTO));

        // Step 1: Customer existence check
        if (!profileServiceClient.isCustomerExist(UUID.fromString(customerAuthId))) {
            throw new ApiException("Invalid or unapproved customer.", "INVALID_CUSTOMER_ID", HttpStatus.BAD_REQUEST);
        }

        log.info("Fetching the customer details based on customer auth Id");

        CustomerDTO customerDTO = profileServiceClient.fetchCustomerIfExists(UUID.fromString(customerAuthId));

        log.info("Checking the appointment availablity according to the selected lawyer schedule");

        // Step 2: Check availability via Profile service
        AvailabilityCheckRequestDTO availabilityCheckRequestDTO = AvailabilityCheckRequestDTO.builder()
                .appointmentDate(LocalDate.parse(stripeDTO.getAppointmentDate()))
                .lawyerId(stripeDTO.getLawyerId())
                .startTime(LocalTime.parse(stripeDTO.getStartTime()))
                .endTime(LocalTime.parse(stripeDTO.getStartTime()).plusMinutes(30))
                .build();

        boolean isAppointmentAvailable = profileServiceClient.checkAppointmentAvailability(availabilityCheckRequestDTO);

        if (!isAppointmentAvailable) {
            throw new ApiException("The selected time slot is not available for the specified lawyer.", "TIME_SLOT_NOT_AVAILABLE", HttpStatus.NOT_FOUND);
        }

        // Step 3: Check if appointment overlaps with existing ones on the same weekday
        DayOfWeek requestedDayOfWeek = LocalDate.parse(stripeDTO.getAppointmentDate()).getDayOfWeek();

        log.info("Checking if the selected lawyer has any existing appointments for lawyer {} on day {}", stripeDTO.getLawyerId(), requestedDayOfWeek.getValue());

//        List<Appointment> existingAppointments = appointmentRepository
//                .findByLawyerIdAndDayOfWeek(stripeDTO.getLawyerId(), requestedDayOfWeek.getValue());

        List<Appointment> existingAppointments = appointmentRepository
                .findByLawyerIdAndAppointmentDate(stripeDTO.getLawyerId(), LocalDate.parse(stripeDTO.getAppointmentDate()));

        checkAppointmentOverlap(LocalTime.parse(stripeDTO.getStartTime()), existingAppointments);

        if (existingAppointments.isEmpty()) {
            log.info("No existing appointment for lawyer {} in the given timeslot", stripeDTO.getLawyerId());
        }

        for (Appointment existing : existingAppointments) {
            // First check if they are on the same date
            if (existing.getAppointmentDate().toString().equals(stripeDTO.getAppointmentDate())) {

                LocalTime newStart = LocalTime.parse(stripeDTO.getStartTime());
                LocalTime newEnd = newStart.plusMinutes(30);

                boolean overlaps = !(newEnd.isBefore(existing.getStartTime())
                        || newStart.isAfter(existing.getEndTime()));

                if (overlaps) {
                    throw new ApiException(
                            "An appointment already exists for the specified time slot.",
                            "APPOINTMENT_ALREADY_EXISTS",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        // Step 4: Prepare and save
//        UUID custId = UUID.fromString(stripeDTO.getCustomer_details().getId());

        log.info("Fetching the appointment type based on the given type name {}", stripeDTO.getAppointmentTypeId());

        String appointmentType = stripeDTO.getAppointmentTypeId();
        LawyerDetailsDTO lawyerDetailsDTO = profileServiceClient.fetchLawyerServices(stripeDTO.getLawyerId());

        log.info("Checking if the lawyer service {} exists", appointmentType);

        boolean exists = lawyerDetailsDTO.getServices().stream()
                .anyMatch(group ->
                        group.getServices() != null &&
                                group.getServices().contains(appointmentType)
                );

        if (!exists) {
            log.error("Appointment type '{}' is not applicable for lawyer {}",
                    appointmentType, stripeDTO.getLawyerId());

            throw new ApiException(
                    "The selected appointment type is not applicable for this lawyer.",
                    "APPOINTMENT_TYPE_NOT_ALLOWED",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("Precheck for the Appointment Type existence");

        AppointmentType appointmentTypeRec = appointmentTypeRepository
                .findByNameIgnoreCase(appointmentType)
                .orElseGet(() -> {
                    AppointmentType newType = AppointmentType.builder()
                            .name(appointmentType)
                            .build();

                    log.info("Storing the Appointment Type received from lawyer profile record {}", newType);
                    return appointmentTypeRepository.save(newType);
                });

        log.info("Storing the appointment record in the database for lawyer {}",  stripeDTO.getLawyerId());

        log.info("Fetching the device token based on selected lawyer with auth Id {}", lawyerDetailsDTO.getAuthUserId());

        String deviceToken = authServiceClient.getDeviceToken(lawyerDetailsDTO.getAuthUserId());

        log.info("Sending the notification to the user device");

        try {
            notificationService.sendNotificationToDevice(deviceToken, "Appointment Booking", "A customer wants to book an appointment with you.");
        } catch (Exception ex) {
            log.error("Failed to send notification to device: {}", ex.getMessage());
        }

        Appointment savedAppointment = Appointment.builder()
                .customerId(customerDTO.getId())
                .lawyerId(stripeDTO.getLawyerId())
                .appointmentDate(LocalDate.parse(stripeDTO.getAppointmentDate()))
                .startTime(LocalTime.parse(stripeDTO.getStartTime()))
                .endTime(LocalTime.parse(stripeDTO.getStartTime()).plusMinutes(30))
                .description(stripeDTO.getDescription())
                .status(AppointmentStatus.PENDING)
                .appointmentType(appointmentTypeRec)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        appointmentRepository.save(savedAppointment);

        log.info("Appointment saved successfully, Storing the transaction record in the database");

        Transaction savedTransaction = Transaction.builder()
                .appointment(savedAppointment)
                .amountTotal(stripeDTO.getAmount_total())
                .currency(stripeDTO.getCurrency())
                .paymentStatus(stripeDTO.getPayment_status())
                .paymentTransactionId(stripeDTO.getPayment_transaction_id())
                .paymentMethodType(String.join(",", stripeDTO.getPayment_method_types()))
                .customerDetails(new ObjectMapper().valueToTree(stripeDTO.getCustomer_details()))
                .createdAt(Instant.ofEpochSecond(stripeDTO.getCreated()).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        transactionRepository.save(savedTransaction);

        log.info("Transaction recorded successfully for user {}", stripeDTO.getCustomer_details().getEmail());

        savedAppointment.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(savedAppointment);

        log.info("Appointment {} status updated to BOOKED", savedAppointment.getId());

//        log.info("Sending email notification to lawyer {}", lawyerDetailsDTO.getLawyerEmail());
//        emailService.sendAppointmentBookingEmail(lawyerDetailsDTO.getLawyerEmail(),
//                stripeDTO.getCustomer_details().getName(),
//                appointmentType, stripeDTO.getAppointmentDate(), stripeDTO.getStartTime());

        return savedAppointment;
    }

//    @Transactional
//    public Appointment bookAppointment(StripeDTO stripeDTO) {
//
//        // Step 1: Customer existence check
////        if (!profileServiceClient.isCustomerExist(UUID.fromString(stripeDTO.getCustomer_details().getId()))) {
////            throw new ApiException("Invalid or unapproved customer.", "INVALID_CUSTOMER_ID", HttpStatus.BAD_REQUEST);
////        }
//
//        // Step 2: Check availability via Profile service
//        AvailabilityCheckRequestDTO availabilityCheckRequestDTO = AvailabilityCheckRequestDTO.builder()
//                .appointmentDate(LocalDate.parse(stripeDTO.getAppointmentDate()))
//                .lawyerId(stripeDTO.getLawyerId())
//                .startTime(LocalTime.parse(stripeDTO.getStartTime()))
//                .endTime(LocalTime.parse(stripeDTO.getStartTime()).plusMinutes(30))
//                .build();
//
//        boolean isAppointmentAvailable = profileServiceClient.checkAppointmentAvailability(availabilityCheckRequestDTO);
//
//        if (!isAppointmentAvailable) {
//            throw new ApiException("The selected time slot is not available for the specified lawyer.", "TIME_SLOT_NOT_AVAILABLE", HttpStatus.NOT_FOUND);
//        }
//
//        // Step 3: Check if appointment overlaps with existing ones on the same weekday
//        DayOfWeek requestedDayOfWeek = LocalDate.parse(stripeDTO.getAppointmentDate()).getDayOfWeek();
//
//        List<Appointment> existingAppointments = appointmentRepository
//                .findByLawyerIdAndDayOfWeek(stripeDTO.getLawyerId(), requestedDayOfWeek.getValue());
//
//        for (Appointment existing : existingAppointments) {
//            boolean overlaps = !(LocalTime.parse(stripeDTO.getStartTime()).plusMinutes(30).isBefore(existing.getStartTime())
//                    || LocalTime.parse(stripeDTO.getStartTime()).isAfter(existing.getEndTime()));
//            if (overlaps) {
//                throw new ApiException("An appointment already exists for the specified time slot.", "APPOINTMENT_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
//            }
//        }
//
//        // Step 4: Prepare and save
//        UUID customerId = UUID.fromString(stripeDTO.getCustomer_details().getId());
//
//        AppointmentType appointmentType = appointmentTypeRepository.findByName(stripeDTO.getAppointmentTypeId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment type"));
//
//        Appointment savedAppointment = Appointment.builder()
//                .customerId(customerId)
//                .lawyerId(stripeDTO.getLawyerId())
//                .appointmentDate(LocalDate.parse(stripeDTO.getAppointmentDate()))
//                .startTime(LocalTime.parse(stripeDTO.getStartTime()))
//                .endTime(LocalTime.parse(stripeDTO.getStartTime()).plusMinutes(30))
//                .description(stripeDTO.getDescription())
//                .status(AppointmentStatus.PENDING)
//                .appointmentType(appointmentType)
//                .build();
//        appointmentRepository.save(savedAppointment);
//
//        Transaction savedTransaction = Transaction.builder()
//                .appointment(savedAppointment)
//                .amountTotal(stripeDTO.getAmount_total())
//                .currency(stripeDTO.getCurrency())
//                .paymentStatus(stripeDTO.getPayment_status())
//                .paymentTransactionId(stripeDTO.getPayment_transaction_id())
//                .paymentMethodType(String.join(",", stripeDTO.getPayment_method_types()))
//                .customerDetails(new ObjectMapper().valueToTree(stripeDTO.getCustomer_details()))
//                .createdAt(Instant.ofEpochSecond(stripeDTO.getCreated()).atZone(ZoneId.systemDefault()).toLocalDateTime())
//                .build();
//
//        transactionRepository.save(savedTransaction);
//
//        log.info("Appointment booked and transaction recorded for user {}", stripeDTO.getCustomer_details().getEmail());
//
//        return savedAppointment;
//    }

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

        AppointmentType type = appointmentTypeRepository.findByNameIgnoreCase(appointmentOpenRequestDTO.getAppointmentType())
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

        log.info("Fetching appointments for customer auth id: {}", customerAuthUserId);

        UUID customerAuthId;
        try {
            customerAuthId = UUID.fromString(customerAuthUserId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid customer auth user ID format: {}", customerAuthUserId, e);
            throw new ApiException(
                    "Invalid customer authentication ID format.",
                    "INVALID_CUSTOMER_ID",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Fetch customer profile or handle if missing
        CustomerDTO customer;
        try {
            customer = profileServiceClient.fetchCustomerIfExists(customerAuthId);
            if (customer == null) {
                log.warn("Customer with auth user ID {} does not exist", customerAuthId);
                throw new ApiException(
                        "Customer not authorized to list the appointments.",
                        "CUSTOMER_NOT_AUTHORIZED",
                        HttpStatus.UNAUTHORIZED
                );
            }
        } catch (Exception e) {
            log.error("Error while verifying customer existence for ID {}: {}", customerAuthId, e.getMessage(), e);
            throw new ApiException(
                    "Failed to validate customer. Please try again later.",
                    "CUSTOMER_LOOKUP_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        log.info("Customer {} exists. Fetching appointments...", customerAuthId);

        return appointmentRepository.findAllByCustomerId(customerAuthId);
    }

    public List<Appointment> getByLawyer(String lawyerAuthUserId) {

        LawyerDetailsDTO lawyerDetailsDTO = profileServiceClient.fetchLawyerServices(UUID.fromString(lawyerAuthUserId));

        if (!profileServiceClient.isLawyerValid(UUID.fromString(lawyerAuthUserId))) {
            throw new ApiException("lawyer not authorized to list the appointments.", "LAWYER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return appointmentRepository.findAllByLawyerId(lawyerDetailsDTO.getId());
    }

    public List<Appointment> getAppointmentsByLawyerId(UUID lawyerId) {
        if (!profileServiceClient.isLawyerValidByLawyerId(lawyerId)) {
            throw new ApiException("lawyer not authorized to list the appointments.", "LAWYER_NOT_AUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
        return appointmentRepository.findAllByLawyerId(lawyerId);
    }

    public List<AppointmentWithTransactionDTO> getAppointmentsWithTransactionsByCustomerId(UUID customerAuthId, boolean adminFlag) {

        log.info("Fetching the customer details based on customer auth Id {}",  customerAuthId);

        if (!adminFlag) {
            CustomerDTO customer = profileServiceClient.fetchCustomerIfExists(customerAuthId);
            log.info("Fetching the appointment(s) details based on customer Id {}", customer.getId());
            List<Appointment> appointments = appointmentRepository.findAllByCustomerId(customer.getId());

            return appointments.stream().map(appointment -> {
                log.info("Fetching the transactions based on appointment Id {}", appointment.getId());
                Transaction transaction = transactionRepository.findByAppointmentId(appointment.getId())
                        .orElse(null);
                return AppointmentWithTransactionDTO.builder()
                        .appointmentId(appointment.getId())
                        .customerId(appointment.getCustomerId())
                        .lawyerId(appointment.getLawyerId())
                        .appointmentDate(appointment.getAppointmentDate())
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus().name())
                        .description(appointment.getDescription())
                        .appointmentType(appointment.getAppointmentType().getName())
                        .createdAt(appointment.getCreatedAt())
                        .transactionId(transaction != null ? transaction.getId() : null)
                        .amountTotal(transaction != null ? transaction.getAmountTotal() : null)
                        .amountSubtotal(transaction != null ? transaction.getAmountSubtotal() : null)
                        .currency(transaction != null ? transaction.getCurrency() : null)
                        .paymentStatus(transaction != null ? transaction.getPaymentStatus() : null)
                        .paymentTransactionId(transaction != null ? transaction.getPaymentTransactionId() : null)
                        .paymentMethod(transaction != null ? transaction.getPaymentMethodType() : null)
                        .liveMode(transaction != null && transaction.isLivemode())
                        .stripeMode(transaction != null ? transaction.getStripeStatus() : null)
                        .transactionCreatedAt(transaction != null ? transaction.getCreatedAt() : null)
                        .customerDetails(transaction != null ? String.valueOf(transaction.getCustomerDetails()) : null)
                        .build();
            }).toList();
        } else {
            log.info("Fetching the appointment(s) details for ADMIN");
            List<Appointment> appointments = appointmentRepository.findAll();
            return appointments.stream().map(appointment -> {
                log.info("Fetching the transactions based on appointment Id {}", appointment.getId());
                Transaction transaction = transactionRepository.findByAppointmentId(appointment.getId())
                        .orElse(null);
                return AppointmentWithTransactionDTO.builder()
                        .appointmentId(appointment.getId())
                        .customerId(appointment.getCustomerId())
                        .lawyerId(appointment.getLawyerId())
                        .appointmentDate(appointment.getAppointmentDate())
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus().name())
                        .description(appointment.getDescription())
                        .appointmentType(appointment.getAppointmentType().getName())
                        .createdAt(appointment.getCreatedAt())
                        .transactionId(transaction != null ? transaction.getId() : null)
                        .amountTotal(transaction != null ? transaction.getAmountTotal() : null)
                        .amountSubtotal(transaction != null ? transaction.getAmountSubtotal() : null)
                        .currency(transaction != null ? transaction.getCurrency() : null)
                        .paymentStatus(transaction != null ? transaction.getPaymentStatus() : null)
                        .paymentTransactionId(transaction != null ? transaction.getPaymentTransactionId() : null)
                        .paymentMethod(transaction != null ? transaction.getPaymentMethodType() : null)
                        .liveMode(transaction != null && transaction.isLivemode())
                        .stripeMode(transaction != null ? transaction.getStripeStatus() : null)
                        .transactionCreatedAt(transaction != null ? transaction.getCreatedAt() : null)
                        .customerDetails(transaction != null ? String.valueOf(transaction.getCustomerDetails()) : null)
                        .build();
            }).toList();
        }
    }

    public List<AppointmentWithTransactionDTO> getAppointmentsWithTransactionsByLawyerId(UUID lawyerAuthId, boolean adminFlag) {

        log.info("Fetching the lawyer details based on lawyer auth Id {}",  lawyerAuthId);

        if (!adminFlag) {
            UUID lawyerId = profileServiceClient.fetchLawyerId(lawyerAuthId);
            log.info("Fetching the appointment(s) details based on Lawyer Id {}", lawyerId);
            List<Appointment> appointments = appointmentRepository.findAllByLawyerId(lawyerId);

            return appointments.stream().map(appointment -> {
                log.info("Fetching the transactions based on appointment Id {}", appointment.getId());
                Transaction transaction = transactionRepository.findByAppointmentId(appointment.getId())
                        .orElse(null);
                return AppointmentWithTransactionDTO.builder()
                        .appointmentId(appointment.getId())
                        .customerId(appointment.getCustomerId())
                        .lawyerId(appointment.getLawyerId())
                        .appointmentDate(appointment.getAppointmentDate())
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus().name())
                        .description(appointment.getDescription())
                        .appointmentType(appointment.getAppointmentType().getName())
                        .createdAt(appointment.getCreatedAt())
                        .transactionId(transaction != null ? transaction.getId() : null)
                        .amountTotal(transaction != null ? transaction.getAmountTotal() : null)
                        .amountSubtotal(transaction != null ? transaction.getAmountSubtotal() : null)
                        .currency(transaction != null ? transaction.getCurrency() : null)
                        .paymentStatus(transaction != null ? transaction.getPaymentStatus() : null)
                        .paymentTransactionId(transaction != null ? transaction.getPaymentTransactionId() : null)
                        .paymentMethod(transaction != null ? transaction.getPaymentMethodType() : null)
                        .liveMode(transaction != null && transaction.isLivemode())
                        .stripeMode(transaction != null ? transaction.getStripeStatus() : null)
                        .transactionCreatedAt(transaction != null ? transaction.getCreatedAt() : null)
                        .customerDetails(transaction != null ? String.valueOf(transaction.getCustomerDetails()) : null)
                        .build();
            }).toList();
        } else {
            log.info("Fetching the appointment(s) details for ADMIN");
            List<Appointment> appointments = appointmentRepository.findAll();
            return appointments.stream().map(appointment -> {
                log.info("Fetching the transactions based on appointment Id {}", appointment.getId());
                Transaction transaction = transactionRepository.findByAppointmentId(appointment.getId())
                        .orElse(null);
                return AppointmentWithTransactionDTO.builder()
                        .appointmentId(appointment.getId())
                        .customerId(appointment.getCustomerId())
                        .lawyerId(appointment.getLawyerId())
                        .appointmentDate(appointment.getAppointmentDate())
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus().name())
                        .description(appointment.getDescription())
                        .appointmentType(appointment.getAppointmentType().getName())
                        .createdAt(appointment.getCreatedAt())
                        .transactionId(transaction != null ? transaction.getId() : null)
                        .amountTotal(transaction != null ? transaction.getAmountTotal() : null)
                        .amountSubtotal(transaction != null ? transaction.getAmountSubtotal() : null)
                        .currency(transaction != null ? transaction.getCurrency() : null)
                        .paymentStatus(transaction != null ? transaction.getPaymentStatus() : null)
                        .paymentTransactionId(transaction != null ? transaction.getPaymentTransactionId() : null)
                        .paymentMethod(transaction != null ? transaction.getPaymentMethodType() : null)
                        .liveMode(transaction != null && transaction.isLivemode())
                        .stripeMode(transaction != null ? transaction.getStripeStatus() : null)
                        .transactionCreatedAt(transaction != null ? transaction.getCreatedAt() : null)
                        .customerDetails(transaction != null ? String.valueOf(transaction.getCustomerDetails()) : null)
                        .build();
            }).toList();
        }
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
