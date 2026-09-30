package com.bloodbond.controller;

import com.bloodbond.dto.EmergencyView;
import com.bloodbond.model.EmergencyRequest;
import com.bloodbond.model.EmergencyStatus;
import com.bloodbond.model.Role;
import com.bloodbond.model.User;
import com.bloodbond.repository.EmergencyRequestRepository;
import com.bloodbond.repository.UserRepository;
import com.bloodbond.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyRequestRepository emergencyRepository;
    private final UserRepository userRepository;

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping
    public String board(Model model, Principal principal) {
        User me = currentUser(principal);
        LocalDateTime now = LocalDateTime.now();
        List<EmergencyView> requests = emergencyRepository
                .findByStatusOrderByCreatedAtDesc(EmergencyStatus.OPEN).stream()
                .filter(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(now))
                .map(e -> new EmergencyView(e, distance(me, e), minutesLeft(e, now)))
                .toList();
        model.addAttribute("requests", requests);
        model.addAttribute("me", me);
        return "emergency";
    }

    private Double distance(User me, EmergencyRequest e) {
        if (me.getLatitude() == null || me.getLongitude() == null
                || e.getLatitude() == null || e.getLongitude() == null) return null;
        return GeoService.distanceKm(me.getLatitude(), me.getLongitude(), e.getLatitude(), e.getLongitude());
    }

    private Long minutesLeft(EmergencyRequest e, LocalDateTime now) {
        if (e.getExpiresAt() == null) return null;
        return Math.max(0, ChronoUnit.MINUTES.between(now, e.getExpiresAt()));
    }

    @GetMapping("/new")
    public String newRequestPage() {
        return "emergency-form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String patientName,
                         @RequestParam String bloodGroup,
                         @RequestParam int unitsNeeded,
                         @RequestParam String hospitalName,
                         @RequestParam String address,
                         @RequestParam String contactPhone,
                         @RequestParam(required = false) String latitude,
                         @RequestParam(required = false) String longitude,
                         @RequestParam(required = false) String note,
                         @RequestParam(defaultValue = "24") int validForHours,
                         Principal principal,
                         RedirectAttributes ra) {
        User me = currentUser(principal);
        EmergencyRequest er = new EmergencyRequest();
        er.setRequester(me);
        er.setPatientName(patientName);
        er.setBloodGroup(bloodGroup);
        er.setUnitsNeeded(unitsNeeded);
        er.setHospitalName(hospitalName);
        er.setAddress(address);
        er.setContactPhone(contactPhone);
        er.setNote(note);
        if (latitude != null && !latitude.isBlank()) er.setLatitude(Double.parseDouble(latitude));
        if (longitude != null && !longitude.isBlank()) er.setLongitude(Double.parseDouble(longitude));
        er.setCreatedAt(LocalDateTime.now());
        er.setExpiresAt(LocalDateTime.now().plusHours(validForHours));
        emergencyRepository.save(er);
        ra.addFlashAttribute("message", "Emergency request posted! Nearby donors have been alerted.");
        return "redirect:/emergency";
    }

    @PostMapping("/{id}/fulfilled")
    public String fulfilled(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        EmergencyRequest er = emergencyRepository.findById(id).orElseThrow();
        User me = currentUser(principal);
        if (er.getRequester().getId().equals(me.getId()) || me.getRole() == Role.ADMIN) {
            er.setStatus(EmergencyStatus.FULFILLED);
            emergencyRepository.save(er);
            ra.addFlashAttribute("message", "Marked as fulfilled. Thank you for saving a life!");
        }
        return "redirect:/emergency";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        EmergencyRequest er = emergencyRepository.findById(id).orElseThrow();
        User me = currentUser(principal);
        if (er.getRequester().getId().equals(me.getId()) || me.getRole() == Role.ADMIN) {
            emergencyRepository.delete(er);
            ra.addFlashAttribute("message", "Request removed.");
        }
        return "redirect:/emergency";
    }
}
