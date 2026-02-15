package com.appointment.service;

import com.appointment.constants.AppointmentStatus;
import com.appointment.constants.CandidateStatus;
import com.appointment.entities.Appointment;
import com.appointment.entities.AppointmentLawyerCandidate;
import com.appointment.repositories.AppointmentLawyerCandidateRepository;
import com.appointment.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentCandidateService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentLawyerCandidateRepository candidateRepository;

    // Called when open appointment is created
    public void createCandidates(Appointment appointment, List<UUID> lawyerIds) {
        log.info("Started creating appointment candidates for {} available and free lawyers", lawyerIds.size());
        List<AppointmentLawyerCandidate> candidates = lawyerIds.stream()
                .map(lawyerId -> AppointmentLawyerCandidate.builder()
                        .appointment(appointment)
                        .lawyerId(lawyerId)
                        .status(CandidateStatus.PENDING)
                        .build())
                .toList();

        candidateRepository.saveAll(candidates);
        log.info("Saved the appointment candidates for {} lawyers", lawyerIds.size());
    }

    // Lawyer sees pending requests
    public List<Appointment> getPendingAppointmentsForLawyer(UUID lawyerId) {
        return candidateRepository.findByLawyerIdAndStatus(lawyerId, CandidateStatus.PENDING)
                .stream()
                .map(AppointmentLawyerCandidate::getAppointment)
                .toList();
    }

    // Lawyer accepts appointment
    public void acceptAppointment(UUID appointmentId, UUID lawyerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Appointment already assigned");
        }

        AppointmentLawyerCandidate candidate =
                candidateRepository.findByAppointmentIdAndLawyerId(appointmentId, lawyerId)
                        .orElseThrow(() -> new RuntimeException("You are not a candidate for this appointment"));

        // Assign lawyer
        appointment.setLawyerId(lawyerId);
        appointment.setStatus(AppointmentStatus.ACCEPTED);
        appointmentRepository.save(appointment);

        // Update candidate statuses
        List<AppointmentLawyerCandidate> allCandidates =
                candidateRepository.findByAppointmentId(appointmentId);

        for (AppointmentLawyerCandidate c : allCandidates) {
            if (c.getLawyerId().equals(lawyerId)) {
                c.setStatus(CandidateStatus.ACCEPTED);
            } else {
                c.setStatus(CandidateStatus.DECLINED);
            }
        }

        candidateRepository.saveAll(allCandidates);
    }

    public void declineAppointment(UUID appointmentId, UUID lawyerId) {
        AppointmentLawyerCandidate candidate =
                candidateRepository.findByAppointmentIdAndLawyerId(appointmentId, lawyerId)
                        .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setStatus(CandidateStatus.DECLINED);
        candidateRepository.save(candidate);
    }
}