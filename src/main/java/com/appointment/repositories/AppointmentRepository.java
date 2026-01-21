package com.appointment.repositories;

import com.appointment.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query(value = "SELECT * FROM appointment a " +
            "WHERE a.lawyer_id = :lawyerId " +
            "AND EXTRACT(DOW FROM a.appointment_date) = :dayOfWeek",
            nativeQuery = true)
    List<Appointment> findByLawyerIdAndDayOfWeek(
            @Param("lawyerId") UUID lawyerId,
            @Param("dayOfWeek") int dayOfWeek);

    List<Appointment> findByLawyerIdAndAppointmentDate(UUID lawyerId, LocalDate appointmentDate);

    List<Appointment> findAllByCustomerId(UUID customerId);
    List<Appointment> findAll();
    List<Appointment> findAllByLawyerId(UUID lawyerId);

    @Query("""
        SELECT DISTINCT a.lawyerId
        FROM Appointment a
        WHERE a.lawyerId IN :lawyerIds
          AND a.appointmentDate = :date
          AND a.status IN ('BOOKED', 'PENDING')
          AND (
                :startTime < a.endTime
            AND :endTime > a.startTime
          )
    """)
    List<UUID> findBusyLawyerIds(
            @Param("lawyerIds") List<UUID> lawyerIds,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
