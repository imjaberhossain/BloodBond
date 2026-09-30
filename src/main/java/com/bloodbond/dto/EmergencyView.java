package com.bloodbond.dto;

import com.bloodbond.model.EmergencyRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmergencyView {
    private EmergencyRequest request;
    private Double distanceKm;
    private Long minutesLeft;
}
