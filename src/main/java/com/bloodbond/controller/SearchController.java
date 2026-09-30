package com.bloodbond.controller;

import com.bloodbond.dto.DonorResult;
import com.bloodbond.model.User;
import com.bloodbond.repository.UserRepository;
import com.bloodbond.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final UserRepository userRepository;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String bloodGroup,
                         Model model, Principal principal) {
        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

        boolean locationReady = me.getLatitude() != null && me.getLongitude() != null;
        String group = (bloodGroup == null || bloodGroup.isBlank()) ? null : bloodGroup;

        List<DonorResult> results = userRepository.findDonors(group, me.getId()).stream()
                .map(u -> {
                    Double d = null;
                    if (locationReady && u.getLatitude() != null && u.getLongitude() != null) {
                        d = GeoService.distanceKm(me.getLatitude(), me.getLongitude(),
                                u.getLatitude(), u.getLongitude());
                    }
                    return new DonorResult(u, d);
                })
                .sorted(Comparator.comparing(DonorResult::getDistanceKm,
                        Comparator.nullsLast(Double::compareTo)))
                .toList();

        model.addAttribute("results", results);
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("locationReady", locationReady);
        return "search";
    }
}
