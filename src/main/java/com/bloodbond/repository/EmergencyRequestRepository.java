package com.bloodbond.repository;

import com.bloodbond.model.EmergencyRequest;
import com.bloodbond.model.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {

    List<EmergencyRequest> findByStatusOrderByCreatedAtDesc(EmergencyStatus status);

    long countByStatus(EmergencyStatus status);

    @Modifying
    @Query("update EmergencyRequest e set e.status = com.bloodbond.model.EmergencyStatus.EXPIRED " +
            "where e.status = com.bloodbond.model.EmergencyStatus.OPEN and e.expiresAt < :now")
    int expireOldRequests(@Param("now") LocalDateTime now);
}
