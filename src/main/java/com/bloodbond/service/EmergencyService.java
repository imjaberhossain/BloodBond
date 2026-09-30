package com.bloodbond.service;

import com.bloodbond.repository.EmergencyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyRequestRepository emergencyRepository;

    /** Enterprise touch: emergency posts auto-expire (checked every 60 seconds). */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoExpireRequests() {
        emergencyRepository.expireOldRequests(LocalDateTime.now());
    }
}
