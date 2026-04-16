package com.example.tckt.demo.scheduler;

import com.example.tckt.demo.dto.TicketDTO;
import com.example.tckt.demo.entity.Ticket;
import com.example.tckt.demo.repository.TicketRepository;
import com.example.tckt.demo.repository.UserRepository;
import com.example.tckt.demo.service.NotificationService;
import com.example.tckt.demo.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TicketService ticketService;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    

    // @Scheduled(fixedRate = 40 * 60 * 1000)  // every 30 minutes in milliseconds
    @Transactional(readOnly = true)
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now(IST_ZONE);
        log.info("SLA check running at {}", now);

        checkResponseSlaBreaches(now);
        checkResolveSlaBreaches(now);
    }

   

    private void checkResponseSlaBreaches(LocalDateTime now) {
        List<Ticket> breached = ticketRepository.findResponseSlaBreached(now);

        if (breached.isEmpty()) {
            log.debug("No response SLA breaches found");
            return;
        }

        log.warn("Found {} response SLA breach(es)", breached.size());

        breached.forEach(ticket -> {
            log.warn("Response SLA breached — ticket: {} priority: {}",
                    ticket.getTicketNumber(), ticket.getPriority());

            // Notify all admins
            notifyAllAdmins(ticket, "Response SLA Breached");
        });
    }

    // ─────────────────────────────────────────────
    // Resolve SLA — ticket not resolved in time
    // ─────────────────────────────────────────────

    private void checkResolveSlaBreaches(LocalDateTime now) {
        List<Ticket> breached = ticketRepository.findResolveSlaBreached(now);

        if (breached.isEmpty()) {
            log.debug("No resolve SLA breaches found");
            return;
        }

        log.warn("Found {} resolve SLA breach(es)", breached.size());

        breached.forEach(ticket -> {
            log.warn("Resolve SLA breached — ticket: {} priority: {}",
                    ticket.getTicketNumber(), ticket.getPriority());

            // Notify all admins
            notifyAllAdmins(ticket, "Resolve SLA Breached");
        });
    }

    // ─────────────────────────────────────────────
    // Helper — notify all admins about breach
    // ─────────────────────────────────────────────

    private void notifyAllAdmins(Ticket ticket, String breachType) {
        try {
            TicketDTO dto = ticketService.getTicketById(ticket.getId()).orElse(null);
            if (dto == null) return;

            // Get all admin emails and send alert to each
            userRepository.findByRole(com.example.tckt.demo.entity.User.UserRole.ADMIN)
                    .forEach(admin -> {
                        try {
                            notificationService.sendSlaBreachAlert(
                                    admin.getEmail(), dto, breachType);
                        } catch (Exception e) {
                            log.error("Failed to send SLA alert to admin {}: {}",
                                    admin.getEmail(), e.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("SLA breach notification failed for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage());
        }
    }
}