package com.bloodbond.controller;

import com.bloodbond.model.Role;
import com.bloodbond.model.User;
import com.bloodbond.repository.PostRepository;
import com.bloodbond.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SimpMessagingTemplate messaging;

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/users/{id}")
    public String profile(@PathVariable Long id, Model model, Principal principal) {
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isOwner", currentUser(principal).getId().equals(id));
        model.addAttribute("posts", postRepository.findByAuthorOrderByCreatedAtDesc(user));
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editPage(Model model, Principal principal) {
        model.addAttribute("user", currentUser(principal));
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String edit(@RequestParam String fullName,
                       @RequestParam String phone,
                       @RequestParam String bloodGroup,
                       @RequestParam(required = false) String gender,
                       @RequestParam(required = false) String city,
                       @RequestParam(required = false) String dateOfBirth,
                       @RequestParam(required = false) String bio,
                       @RequestParam(required = false) String latitude,
                       @RequestParam(required = false) String longitude,
                       @RequestParam(required = false) Boolean canDonate,
                       @RequestParam(required = false) MultipartFile avatar,
                       Principal principal) throws IOException {
        User me = currentUser(principal);
        me.setFullName(fullName);
        me.setPhone(phone);
        me.setBloodGroup(bloodGroup);
        me.setGender(gender);
        me.setCity(city);
        me.setBio(bio);
        me.setCanDonate(canDonate == null || canDonate);
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            me.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }
        if (latitude != null && !latitude.isBlank()) me.setLatitude(Double.parseDouble(latitude));
        if (longitude != null && !longitude.isBlank()) me.setLongitude(Double.parseDouble(longitude));
        String avatarPath = HomeController.saveImage(avatar, "avatars");
        if (avatarPath != null) me.setAvatarPath(avatarPath);
        userRepository.save(me);
        return "redirect:/users/" + me.getId();
    }

    /** Records a completed donation -> bumps the counter and broadcasts the new badge live. */
    @PostMapping("/profile/donation/record")
    @ResponseBody
    public Map<String, Object> recordDonation(Principal principal) {
        User me = currentUser(principal);
        me.setDonationCount(me.getDonationCount() + 1);
        me.setLastDonationDate(LocalDate.now());
        userRepository.save(me);

        Map<String, Object> stats = new HashMap<>();
        stats.put("donations", me.getDonationCount());
        stats.put("badge", me.getBadge());
        stats.put("badgeEmoji", me.getBadgeEmoji());
        messaging.convertAndSend("/topic/user/" + me.getId() + "/stats", stats);
        return stats;
    }
}
