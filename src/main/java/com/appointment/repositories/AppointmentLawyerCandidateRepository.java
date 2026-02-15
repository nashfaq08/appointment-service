package com.appointment.repositories;

import com.appointment.constants.CandidateStatus;
import com.appointment.entities.AppointmentLawyerCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentLawyerCandidateRepository
        extends JpaRepository<AppointmentLawyerCandidate, UUID> {

    List<AppointmentLawyerCandidate> findByLawyerIdAndStatus(UUID lawyerId, CandidateStatus status);

    Optional<AppointmentLawyerCandidate> findByAppointmentIdAndLawyerId(UUID appointmentId, UUID lawyerId);

    List<AppointmentLawyerCandidate> findByAppointmentId(UUID appointmentId);

    List<AppointmentLawyerCandidate> findByAppointmentId(Long appointmentId);

    Optional<AppointmentLawyerCandidate> findByAppointmentIdAndStatus(Long appointmentId, CandidateStatus status);

}
