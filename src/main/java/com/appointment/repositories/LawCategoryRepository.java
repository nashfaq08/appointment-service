package com.appointment.repositories;

import com.appointment.entities.LawCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LawCategoryRepository extends JpaRepository<LawCategory, Long> {
    List<LawCategory> findByNameContainingIgnoreCase(String name);
}

