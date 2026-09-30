package com.bloodbond.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Demo email sender: in production plug in JavaMailSender / an SMS gateway here.
     * For now the OTP is logged to the server console so you can test instantly.
     */
    public void sendOtp(String email, String code) {
        log.info("========== OTP for {} is: {} (valid 5 minutes) ==========", email, code);
    }
}
