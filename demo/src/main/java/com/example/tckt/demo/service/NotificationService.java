package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.TicketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    // ─────────────────────────────────────────────
    // Ticket created
    // ─────────────────────────────────────────────

    @Async
    public void sendTicketCreatedEmail(String toEmail, TicketDTO ticket) {
        String subject = "Ticket Received: " + ticket.getTicketNumber();
        String body = """
                Hello %s,

                Your IT helpdesk ticket has been received and is being processed.

                Ticket Number : %s
                Title         : %s
                Category      : %s
                Priority      : %s
                Status        : %s

                Our team will respond within the SLA window.
                You can track your ticket status by logging in to the portal.

                Regards,
                IT Helpdesk Team
                """.formatted(
                ticket.getCreatedByName(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getCategory() != null ? ticket.getCategory() : "--",
                ticket.getPriority(),
                ticket.getStatus()
        );
        sendEmail(toEmail, subject, body);
    }

    // ─────────────────────────────────────────────
    // Ticket status changed
    // ─────────────────────────────────────────────

    @Async
    public void sendStatusUpdateEmail(String toEmail, TicketDTO ticket, String comment) {
        String subject = "Update on Ticket: " + ticket.getTicketNumber();
        String body = """
                Hello %s,

                Your ticket status has been updated.

                Ticket Number : %s
                Title         : %s
                New Status    : %s
                %s

                Log in to the portal to view full details.

                Regards,
                IT Helpdesk Team
                """.formatted(
                ticket.getCreatedByName(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getStatus(),
                comment != null && !comment.isBlank()
                        ? "Admin Note   : " + comment
                        : ""
        );
        sendEmail(toEmail, subject, body);
    }

    // ─────────────────────────────────────────────
    // Ticket assigned to agent
    // ─────────────────────────────────────────────

    @Async
    public void sendAssignmentEmail(String toEmail, String agentName, TicketDTO ticket) {
        String subject = "Ticket Assigned to You: " + ticket.getTicketNumber();
        String body = """
                Hello %s,

                A ticket has been assigned to you.

                Ticket Number : %s
                Title         : %s
                Priority      : %s
                Category      : %s

                Please log in to the admin portal to manage this ticket.

                Regards,
                IT Helpdesk System
                """.formatted(
                agentName,
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getPriority(),
                ticket.getCategory() != null ? ticket.getCategory() : "--"
        );
        sendEmail(toEmail, subject, body);
    }

    // ─────────────────────────────────────────────
    // SLA breach alert (for admin)
    // ─────────────────────────────────────────────

    @Async
    public void sendSlaBreachAlert(String toAdminEmail, TicketDTO ticket, String breachType) {
        String subject = "⚠️ SLA Breach Alert: " + ticket.getTicketNumber();
        String body = """
                ALERT: SLA Breach Detected

                Ticket Number : %s
                Title         : %s
                Priority      : %s
                Breach Type   : %s
                Current Status: %s
                Submitted by  : %s

                Immediate action is required.

                IT Helpdesk System
                """.formatted(
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getPriority(),
                breachType,
                ticket.getStatus(),
                ticket.getCreatedByName()
        );
        sendEmail(toAdminEmail, subject, body);
    }

    // ─────────────────────────────────────────────
    // Private helper
    // ─────────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {} — subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to: {} — {}", to, e.getMessage());
            // Never throw — email failure must not crash ticket operations
        }
    }
}