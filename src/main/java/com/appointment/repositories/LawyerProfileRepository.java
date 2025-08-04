package com.appointment.repositories;

import com.appointment.entities.LawyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LawyerProfileRepository extends JpaRepository<LawyerProfile, UUID> {
    boolean existsByAuthUserId(UUID authUserId);

    Optional<LawyerProfile> findByAuthUserId(UUID authUserId);

    @Query("SELECT l FROM LawyerProfile l " +
            "WHERE (:specialityId IS NULL OR l.speciality.id = :specialityId) " +
            "AND (:location IS NULL OR l.location ILIKE %:location%) " +
            "AND (:verified IS NULL OR l.verified = :verified)")
    List<LawyerProfile> findByFilters(@Param("specialityId") Long specialityId,
                                      @Param("location") String location,
                                      @Param("verified") Boolean verified);
}
