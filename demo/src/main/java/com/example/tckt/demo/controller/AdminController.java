package com.example.tckt.demo.controller;

import com.example.tckt.demo.dto.TicketDTO;
import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.entity.Ticket;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.service.AdminService;
import com.example.tckt.demo.service.DepartmentService;
import com.example.tckt.demo.service.TicketService;
import com.example.tckt.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")       // whole controller requires ADMIN role
public class AdminController {

    private final AdminService adminService;
    private final TicketService ticketService;
    private final UserService userService;
    private final DepartmentService departmentService;

    // ─────────────────────────────────────────────
    // Ticket management
    // ─────────────────────────────────────────────

    @GetMapping("/tickets")
    public ResponseEntity<Page<TicketDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
    }

    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<String> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal) {

        try {
            Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status.toUpperCase());
            ticketService.updateTicketStatus(id, ticketStatus);
            log.info("Admin [{}] updated ticket [{}] status to [{}]",
                     principal.getName(), id, status);
            return ResponseEntity.ok("Ticket " + id + " status updated to " + status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid status value: " + status +
                          ". Valid values: OPEN, IN_PROGRESS, RESOLVED, REJECTED, CLOSED");
        }
    }

    @PutMapping("/tickets/{id}/assign")
    public ResponseEntity<String> assignTicket(
            @PathVariable Long id,
            @RequestParam Long agentId,
            Principal principal) {

        ticketService.assignTicket(id, agentId);
        log.info("Admin [{}] assigned ticket [{}] to agent [{}]",
                 principal.getName(), id, agentId);
        return ResponseEntity.ok("Ticket " + id + " assigned to agent " + agentId);
    }

    @PutMapping("/tickets/{id}/process")
    public ResponseEntity<String> processTicket(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Long nextAuthorityId,
            Principal principal) {

        ticketService.processTicket(id, action, comment, nextAuthorityId, principal.getName());
        log.info("Admin [{}] processed ticket [{}] with action [{}]",
                 principal.getName(), id, action);
        return ResponseEntity.ok("Ticket " + id + " processed with action: " + action);
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<String> deleteTicket(
            @PathVariable Long id,
            Principal principal) {

        ticketService.deleteTicket(id);
        log.info("Admin [{}] deleted ticket [{}]", principal.getName(), id);
        return ResponseEntity.ok("Ticket " + id + " deleted.");
    }

    // ─────────────────────────────────────────────
    // Dashboard stats
    // ─────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        return ResponseEntity.ok(Map.of(
            "total",      ticketService.countAllTickets(),
            "open",       ticketService.countByStatus(Ticket.TicketStatus.OPEN),
            "inProgress", ticketService.countByStatus(Ticket.TicketStatus.IN_PROGRESS),
            "resolved",   ticketService.countByStatus(Ticket.TicketStatus.RESOLVED),
            "closed",     ticketService.countByStatus(Ticket.TicketStatus.CLOSED)
        ));
    }

    // ─────────────────────────────────────────────
    // User management
    // ─────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<String> toggleUserActive(
            @PathVariable Long id,
            Principal principal) {

        userService.toggleUserActive(id);
        log.info("Admin [{}] toggled active status for user [{}]", principal.getName(), id);
        return ResponseEntity.ok("User " + id + " active status toggled.");
    }
}