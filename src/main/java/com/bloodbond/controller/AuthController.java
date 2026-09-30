package com.bloodbond.controller;

import com.bloodbond.model.Role;
import com.bloodbond.model.User;
import com.bloodbond.repository.UserRepository;
import com.bloodbond.service.EmailService;
import com.bloodbond.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String phone,
                         @RequestParam String bloodGroup,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam(required = false) String gender,
                         @RequestParam(required = false) String city,
                         @RequestParam(required = false) String dateOfBirth,
                         RedirectAttributes ra) {
        email = email.trim().toLowerCase();
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/signup";
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/auth/signup";
        }
        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null && existing.isVerified()) {
            ra.addFlashAttribute("error", "This email is already registered. Please log in.");
            return "redirect:/auth/login";
        }
        User user = existing == null ? new User() : existing;
        if (existing == null) {
            user.setEmail(email);
            user.setRole(Role.USER);
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setBloodGroup(bloodGroup);
        user.setGender(gender);
        user.setCity(city);
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            user.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        String code = otpService.generate(email);
        emailService.sendOtp(email, code);
        ra.addFlashAttribute("message", "Account created! A 6-digit OTP was sent to your email (also printed in the server console for demo).");
        return "redirect:/auth/verify?email=" + email;
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email, @RequestParam String code, RedirectAttributes ra) {
        if (!otpService.verify(email, code)) {
            ra.addFlashAttribute("error", "Invalid or expired OTP. Please try again or resend.");
            return "redirect:/auth/verify?email=" + email;
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setVerified(true);
        userRepository.save(user);
        ra.addFlashAttribute("message", "Email verified successfully! You can now log in.");
        return "redirect:/auth/login";
    }

    @PostMapping("/resend")
    public String resend(@RequestParam String email, RedirectAttributes ra) {
        String code = otpService.generate(email);
        emailService.sendOtp(email, code);
        ra.addFlashAttribute("message", "A new OTP has been sent (check the server console too).");
        return "redirect:/auth/verify?email=" + email;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // ---------------- FORGOT / RESET PASSWORD ----------------

    @GetMapping("/forgot")
    public String forgotPage() {
        return "auth/forgot";
    }

    @PostMapping("/forgot")
    public String forgot(@RequestParam String email, RedirectAttributes ra) {
        email = email.trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "No account found with this email.");
            return "redirect:/auth/forgot";
        }
        String code = otpService.generate(email);
        emailService.sendOtp(email, code);
        ra.addFlashAttribute("message", "A 6-digit reset code was sent to your email (also printed in the server console for demo).");
        return "redirect:/auth/reset?email=" + email;
    }

    @GetMapping("/reset")
    public String resetPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/reset";
    }

    @PostMapping("/reset")
    public String reset(@RequestParam String email,
                        @RequestParam String code,
                        @RequestParam String password,
                        @RequestParam String confirmPassword,
                        RedirectAttributes ra) {
        email = email.trim().toLowerCase();
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/reset?email=" + email;
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/auth/reset?email=" + email;
        }
        if (!otpService.verify(email, code)) {
            ra.addFlashAttribute("error", "Invalid or expired reset code.");
            return "redirect:/auth/reset?email=" + email;
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(true);
        userRepository.save(user);
        ra.addFlashAttribute("message", "Password reset successful! Log in with your new password.");
        return "redirect:/auth/login";
    }
}
