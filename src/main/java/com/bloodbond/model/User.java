package com.bloodbond.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String phone;

    @Column(nullable = false)
    private String bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private LocalDate dateOfBirth;
    private String gender;
    private String city;

    private Double latitude;
    private Double longitude;

    private String avatarPath;

    @Column(length = 2000)
    private String bio;

    private boolean canDonate = true;

    private LocalDate lastDonationDate;

    private int donationCount = 0;

    private boolean verified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getBadge() {
        if (donationCount >= 25) return "Diamond Guardian";
        if (donationCount >= 15) return "Platinum Hero";
        if (donationCount >= 8) return "Gold Savior";
        if (donationCount >= 3) return "Silver Donor";
        if (donationCount >= 1) return "Bronze Beginner";
        return "New Member";
    }

    public String getBadgeEmoji() {
        if (donationCount >= 25) return "💎";
        if (donationCount >= 15) return "🏆";
        if (donationCount >= 8) return "🥇";
        if (donationCount >= 3) return "🥈";
        if (donationCount >= 1) return "🥉";
        return "✨";
    }

    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
