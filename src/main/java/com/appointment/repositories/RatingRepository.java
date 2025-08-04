package com.appointment.repositories;

import com.appointment.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByLawyer_Id(UUID lawyerId);
}
