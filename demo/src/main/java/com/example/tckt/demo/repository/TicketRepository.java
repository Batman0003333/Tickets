package com.example.tckt.demo.repository;

import com.example.tckt.demo.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ─────────────────────────────────────────────
    // Find by user
    // ─────────────────────────────────────────────

    Page<Ticket> findByCreatedById(Long userId, Pageable pageable);

    Page<Ticket> findByAssignedToId(Long userId, Pageable pageable);

    // ─────────────────────────────────────────────
    // Find by department
    // ─────────────────────────────────────────────

    Page<Ticket> findByDepartmentId(Long departmentId, Pageable pageable);

    // ─────────────────────────────────────────────
    // Find by status and priority
    // ─────────────────────────────────────────────

    Page<Ticket> findByStatus(Ticket.TicketStatus status, Pageable pageable);

    Page<Ticket> findByPriority(Ticket.TicketPriority priority, Pageable pageable);

    Page<Ticket> findByDepartmentIdAndStatus(
            Long departmentId, Ticket.TicketStatus status, Pageable pageable);

    // ─────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────

    Page<Ticket> findByTicketNumberContainingIgnoreCaseOrTitleContainingIgnoreCase(
            String ticketNumber, String title, Pageable pageable);

    // ─────────────────────────────────────────────
    // Counts (for dashboard stats)
    // ─────────────────────────────────────────────

    long countByStatus(Ticket.TicketStatus status);

    long countByCreatedById(Long userId);

    long countByDepartmentId(Long departmentId);

    long countByAssignedToIdAndStatus(Long agentId, Ticket.TicketStatus status);

    // ─────────────────────────────────────────────
    // SLA breach detection
    // ─────────────────────────────────────────────

    // Tickets where response SLA has been breached and ticket is still open
    @Query("SELECT t FROM Ticket t WHERE t.slaResponseDueAt < :now " +
           "AND t.status = 'OPEN' AND t.firstResponseAt IS NULL")
    List<Ticket> findResponseSlaBreached(@Param("now") LocalDateTime now);

    // Tickets where resolve SLA has been breached and ticket is not yet resolved
    @Query("SELECT t FROM Ticket t WHERE t.slaResolveDueAt < :now " +
           "AND t.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    List<Ticket> findResolveSlaBreached(@Param("now") LocalDateTime now);

    // ─────────────────────────────────────────────
    // Agent workload (for round-robin assignment)
    // ─────────────────────────────────────────────

    // Count how many open tickets an agent currently has — used for load balancing
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.id = :agentId " +
           "AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveTicketsByAgent(@Param("agentId") Long agentId);

    long countByCreatedByIdAndStatus(Long userId, Ticket.TicketStatus status);
}