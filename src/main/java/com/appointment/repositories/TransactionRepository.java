package com.appointment.repositories;

import com.appointment.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByAppointmentId(UUID appointmentId);

}
