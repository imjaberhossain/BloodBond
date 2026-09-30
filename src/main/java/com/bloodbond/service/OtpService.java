package com.bloodbond.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final long TTL_MINUTES = 5;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Otp> store = new ConcurrentHashMap<>();

    public String generate(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(email, new Otp(code, Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES)));
        return code;
    }

    public boolean verify(String email, String code) {
        Otp otp = store.get(email.toLowerCase());
        if (otp == null) return false;
        if (Instant.now().isAfter(otp.expiresAt())) {
            store.remove(email);
            return false;
        }
        boolean ok = otp.code().equals(code);
        if (ok) store.remove(email);
        return ok;
    }

    private record Otp(String code, Instant expiresAt) {}
}
