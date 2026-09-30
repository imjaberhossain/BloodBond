package com.bloodbond.dto;

import com.bloodbond.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DonorResult {
    private User user;
    private Double distanceKm;
}
