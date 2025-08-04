package com.appointment.repositories;

import com.appointment.entities.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
//    List<RequestHistory> findByCustomer_CustId(Long custId);
}

