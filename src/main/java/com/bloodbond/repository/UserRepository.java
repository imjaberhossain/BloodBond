package com.bloodbond.repository;

import com.bloodbond.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByVerifiedTrue();

    @Query("select coalesce(sum(u.donationCount), 0) from User u")
    long sumDonations();

    @Query("select u from User u where u.verified = true and u.canDonate = true " +
            "and u.id <> :excludeId and (:bloodGroup is null or u.bloodGroup = :bloodGroup)")
    List<User> findDonors(@Param("bloodGroup") String bloodGroup, @Param("excludeId") Long excludeId);
}
