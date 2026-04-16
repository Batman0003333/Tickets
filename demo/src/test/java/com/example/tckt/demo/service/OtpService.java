package com.example.tckt.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final JavaMailSender mailSender;

    // In-memory store: email -> OtpEntry
    // ConcurrentHashMap is thread-safe for concurrent requests
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    // ─────────────────────────────────────────────
    // Generate and send OTP
    // ─────────────────────────────────────────────

    public void generateAndSendOtp(String email, String firstName) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", (int)(Math.random() * 1000000));

        // Store with expiry
        otpStore.put(email, new OtpEntry(otp, LocalDateTime.now()
                .plusMinutes(OTP_EXPIRY_MINUTES)));

        log.info("OTP generated for email: {}", email);

        // Send email
        sendOtpEmail(email, firstName, otp);
    }

    // ─────────────────────────────────────────────
    // Verify OTP
    // ─────────────────────────────────────────────

    public OtpResult verifyOtp(String email, String enteredOtp) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            return OtpResult.NOT_FOUND;
        }

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            return OtpResult.EXPIRED;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            otpStore.remove(email);
            return OtpResult.MAX_ATTEMPTS;
        }

        if (!entry.otp().equals(enteredOtp.trim())) {
            // Increment attempts
            otpStore.put(email, new OtpEntry(
                    entry.otp(),
                    entry.expiresAt(),
                    entry.attempts() + 1
            ));
            return OtpResult.INVALID;
        }

        // Success — remove from store
        otpStore.remove(email);
        return OtpResult.SUCCESS;
    }

    // ─────────────────────────────────────────────
    // Resend OTP
    // ─────────────────────────────────────────────

    public boolean canResend(String email) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) return true;
        // Allow resend only after 1 minute
        return LocalDateTime.now().isAfter(
                entry.expiresAt().minusMinutes(OTP_EXPIRY_MINUTES - 1));
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    private void sendOtpEmail(String to, String firstName, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your Verification Code — IT Helpdesk");
            message.setText("""
                    Hello %s,

                    Your email verification code is:

                         %s

                    This code expires in %d minutes.
                    Do not share this code with anyone.

                    If you did not request this, please ignore this email.

                    IT Helpdesk Team
                    """.formatted(firstName, otp, OTP_EXPIRY_MINUTES));
            mailSender.send(message);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please check your email address.");
        }
    }

    // ─────────────────────────────────────────────
    // Inner types
    // ─────────────────────────────────────────────

    public record OtpEntry(String otp, LocalDateTime expiresAt, int attempts) {
        public OtpEntry(String otp, LocalDateTime expiresAt) {
            this(otp, expiresAt, 0);
        }
    }

    public enum OtpResult {
        SUCCESS, INVALID, EXPIRED, NOT_FOUND, MAX_ATTEMPTS
    }
}