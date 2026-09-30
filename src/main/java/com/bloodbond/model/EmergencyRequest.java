package com.bloodbond.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "emergency_requests")
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String bloodGroup;

    @Column(nullable = false)
    private int unitsNeeded;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false, length = 1000)
    private String address;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String contactPhone;

    @Column(length = 2000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status = EmergencyStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt;
}
