package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.TicketDTO;
import com.example.tckt.demo.entity.Comment;
import com.example.tckt.demo.entity.Ticket;
import com.example.tckt.demo.entity.TicketHistory;
import com.example.tckt.demo.entity.User;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.repository.CommentRepository;
import com.example.tckt.demo.repository.DepartmentRepository;
import com.example.tckt.demo.repository.TicketHistoryRepository;
import com.example.tckt.demo.repository.TicketRepository;
import com.example.tckt.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CommentRepository commentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Ticket ticket = new Ticket();
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setPriority(ticketDTO.getPriority());
        ticket.setCategory(ticketDTO.getCategory() != null
                ? ticketDTO.getCategory() : "General");
        ticket.setSubCategory(ticketDTO.getSubCategory());
        ticket.setAffectedAsset(ticketDTO.getAffectedAsset());
        ticket.setAffectedUrl(ticketDTO.getAffectedUrl());
        ticket.setStepsToReproduce(ticketDTO.getStepsToReproduce());
        ticket.setReferenceId(ticketDTO.getReferenceId());
        ticket.setStatus(Ticket.TicketStatus.OPEN);

        long count = ticketRepository.count() + 1;
        String year = String.valueOf(LocalDateTime.now(IST_ZONE).getYear());
        ticket.setTicketNumber(String.format("TKT-%s-%06d", year, count));

        User user = userRepository.findById(ticketDTO.getCreatedById())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + ticketDTO.getCreatedById()));
        ticket.setCreatedBy(user);

        if (ticketDTO.getDepartmentId() != null) {
            departmentRepository.findById(ticketDTO.getDepartmentId())
                    .ifPresent(ticket::setDepartment);
        } else if (user.getDepartment() != null) {
            ticket.setDepartment(user.getDepartment());
        }

        ticket.setSlaResponseDueAt(calcResponseSla(ticket.getPriority()));
        ticket.setSlaResolveDueAt(calcResolveSla(ticket.getPriority()));

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: {} by user: {}", saved.getTicketNumber(), user.getEmail());

        // Notify user — async so it never blocks ticket creation
        try {
            notificationService.sendTicketCreatedEmail(user.getEmail(), convertToDTO(saved));
        } catch (Exception e) {
            log.warn("Creation notification failed for ticket {}: {}",
                    saved.getTicketNumber(), e.getMessage());
        }

        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────

    public Page<TicketDTO> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Optional<TicketDTO> getTicketById(Long id) {
        return ticketRepository.findById(id).map(this::convertToDTO);
    }

    public Page<TicketDTO> getTicketsByUser(Long userId, Pageable pageable) {
        return ticketRepository.findByCreatedById(userId, pageable).map(this::convertToDTO);
    }

    public Page<TicketDTO> getAssignedTickets(Long userId, Pageable pageable) {
        return ticketRepository.findByAssignedToId(userId, pageable).map(this::convertToDTO);
    }

    public Page<TicketDTO> getTicketsByStatus(Ticket.TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(this::convertToDTO);
    }

    // ─────────────────────────────────────────────
    // COUNTS
    // ─────────────────────────────────────────────

    public long countAllTickets() {
        return ticketRepository.count();
    }

    public long countByStatus(Ticket.TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    public long countByUserId(Long userId) {
        return ticketRepository.countByCreatedById(userId);
    }

    public long countByUserIdAndStatus(Long userId, String status) {
        Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status.toUpperCase());
        return ticketRepository.countByCreatedByIdAndStatus(userId, ticketStatus);
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    @Transactional
    public TicketDTO updateTicketStatus(Long ticketId, Ticket.TicketStatus status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found: " + ticketId));

        String oldStatus = ticket.getStatus().name();
        ticket.setStatus(status);

        LocalDateTime now = LocalDateTime.now(IST_ZONE);
        if (status == Ticket.TicketStatus.RESOLVED) ticket.setResolvedAt(now);
        if (status == Ticket.TicketStatus.CLOSED)   ticket.setClosedAt(now);

        return convertToDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDTO assignTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found: " + ticketId));

        User agent = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(LocalDateTime.now(IST_ZONE));
        }

        ticket.setAssignedTo(agent);
        ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);

        Ticket saved = ticketRepository.save(ticket);

        // Notify agent about assignment
        try {
            notificationService.sendAssignmentEmail(
                    agent.getEmail(), agent.getFirstName(), convertToDTO(saved));
        } catch (Exception e) {
            log.warn("Assignment notification failed: {}", e.getMessage());
        }

        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────
    // PROCESS (approve / reject / escalate / close)
    // ─────────────────────────────────────────────

    @Transactional
    public void processTicket(Long ticketId, String action,
                               String commentText, Long nextAuthorityId,
                               String adminEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found: " + ticketId));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Admin not found: " + adminEmail));

        // Save audit comment
        if (commentText != null && !commentText.isBlank()) {
            Comment comment = new Comment();
            comment.setContent(commentText);
            comment.setTicket(ticket);
            comment.setCreatedBy(admin);
            comment.setIsInternal(false);
            comment.setCreatedAt(LocalDateTime.now(IST_ZONE));
            commentRepository.save(comment);
        }

        String oldStatus = ticket.getStatus().name();
        LocalDateTime now = LocalDateTime.now(IST_ZONE);

        switch (action.toUpperCase()) {
            case "APPROVE":
                ticket.setStatus(Ticket.TicketStatus.RESOLVED);
                ticket.setResolvedAt(now);
                break;
            case "REJECT":
                ticket.setStatus(Ticket.TicketStatus.REJECTED);
                break;
            case "CLOSE":
                ticket.setStatus(Ticket.TicketStatus.CLOSED);
                ticket.setClosedAt(now);
                break;
            case "ESCALATE":
                if (nextAuthorityId != null) {
                    User nextAuth = userRepository.findById(nextAuthorityId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Escalation target not found: " + nextAuthorityId));
                    ticket.setAssignedTo(nextAuth);
                }
                ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

        ticketRepository.save(ticket);

        // Record history
        recordHistory(ticket, admin, "status", oldStatus, ticket.getStatus().name());
        log.info("Ticket [{}] processed — action: {} by admin: {}",
                ticketId, action, adminEmail);

        // Notify ticket creator about status change — async
        try {
            String creatorEmail = ticket.getCreatedBy().getEmail();
            notificationService.sendStatusUpdateEmail(
                    creatorEmail, convertToDTO(ticket), commentText);
        } catch (Exception e) {
            log.warn("Status notification failed for ticket {}: {}",
                    ticketId, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    @Transactional
    public boolean deleteTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException("Ticket not found: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
        log.info("Ticket [{}] deleted", ticketId);
        return true;
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    private void recordHistory(Ticket ticket, User changedBy,
                                String field, String oldValue, String newValue) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(changedBy);
        history.setFieldChanged(field);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        ticketHistoryRepository.save(history);
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setCategory(ticket.getCategory());
        dto.setSubCategory(ticket.getSubCategory());
        dto.setAffectedAsset(ticket.getAffectedAsset());
        dto.setAffectedUrl(ticket.getAffectedUrl());
        dto.setReferenceId(ticket.getReferenceId());

        if (ticket.getCreatedBy() != null) {
            dto.setCreatedById(ticket.getCreatedBy().getId());
            dto.setCreatedByName(ticket.getCreatedBy().getFirstName()
                    + " " + ticket.getCreatedBy().getLastName());
        }

        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
            dto.setAssignedToName(ticket.getAssignedTo().getFirstName()
                    + " " + ticket.getAssignedTo().getLastName());
        }

        if (ticket.getDepartment() != null) {
            dto.setDepartmentId(ticket.getDepartment().getId());
            dto.setDepartmentName(ticket.getDepartment().getName());
        }

        dto.setCreatedAtStr(formatDate(ticket.getCreatedAt()));
        dto.setUpdatedAtStr(formatDate(ticket.getUpdatedAt()));
        dto.setResolvedAtStr(formatDate(ticket.getResolvedAt()));
        dto.setClosedAtStr(formatDate(ticket.getClosedAt()));
        dto.setSlaResponseDueAtStr(formatDate(ticket.getSlaResponseDueAt()));
        dto.setSlaResolveDueAtStr(formatDate(ticket.getSlaResolveDueAt()));

        if (ticket.getCreatedAt() != null) {
            dto.setCreatedAt(java.sql.Timestamp.valueOf(ticket.getCreatedAt()));
        }

        return dto;
    }

    private String formatDate(LocalDateTime dt) {
        return (dt != null) ? dt.format(DATE_FORMATTER) : null;
    }

    private LocalDateTime calcResponseSla(Ticket.TicketPriority priority) {
        LocalDateTime now = LocalDateTime.now(IST_ZONE);
        return switch (priority) {
            case CRITICAL -> now.plusHours(1);
            case HIGH     -> now.plusHours(2);
            case MEDIUM   -> now.plusHours(4);
            case LOW      -> now.plusHours(8);
        };
    }

    private LocalDateTime calcResolveSla(Ticket.TicketPriority priority) {
        LocalDateTime now = LocalDateTime.now(IST_ZONE);
        return switch (priority) {
            case CRITICAL -> now.plusHours(4);
            case HIGH     -> now.plusHours(8);
            case MEDIUM   -> now.plusHours(24);
            case LOW      -> now.plusHours(72);
        };
    }
}