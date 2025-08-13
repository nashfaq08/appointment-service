package com.appointment.repositories;

import com.appointment.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    List<Appointment> findAllByCustomerId(UUID customerId);
    List<Appointment> findAllByLawyerId(UUID lawyerId);
}
