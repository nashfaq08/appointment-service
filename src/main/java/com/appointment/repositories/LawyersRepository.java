package com.appointment.repositories;

import com.appointment.entities.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LawyersRepository extends JpaRepository<Lawyer, Long> {
//    List<Lawyer> findBySpeciality_Name(String specialityName);
    List<Lawyer> findByServices_Name(String serviceName);
    Optional<Lawyer> findByBarId(String barId);
}

