package com.appointment.repositories;

import com.appointment.entities.Lawyer;
import com.appointment.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String refreshToken);
    void deleteByLawyer(Lawyer lawyer);
}
