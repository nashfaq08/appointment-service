package com.appointment.repositories;

import com.appointment.entities.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {

    Optional<AppointmentType> findByNameIgnoreCase(String name);

}
