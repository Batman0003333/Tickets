package com.example.tckt.demo.dto;

import com.example.tckt.demo.entity.Ticket.TicketPriority;
import com.example.tckt.demo.entity.Ticket.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    // ─────────────────────────────────────────────
    // Core identity
    // ─────────────────────────────────────────────
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;

    // ─────────────────────────────────────────────
    // Category & context
    // ─────────────────────────────────────────────
    private String category;
    private String subCategory;
    private String affectedAsset;
    private String affectedUrl;
    private String stepsToReproduce;
    private String referenceId;

    // ─────────────────────────────────────────────
    // People
    // ─────────────────────────────────────────────
    private Long createdById;
    private String createdByName;
    private Long assignedToId;
    private String assignedToName;
    private String pendingWith;
    private String requestedBy;
    private String lastUpdatedBy;

    // ─────────────────────────────────────────────
    // Department
    // ─────────────────────────────────────────────
    private Long departmentId;
    private String departmentName;

    // ─────────────────────────────────────────────
    // Comments & notes
    // ─────────────────────────────────────────────
    private Integer commentCount;
    private String comments;

    // ─────────────────────────────────────────────
    // Timestamps (Timestamp type — for form binding)
    // ─────────────────────────────────────────────
    private Timestamp createdAt;

    // ─────────────────────────────────────────────
    // Timestamps (formatted strings — for display in IST)
    // ─────────────────────────────────────────────
    private String createdAtStr;
    private String updatedAtStr;
    private String resolvedAtStr;
    private String closedAtStr;
    private String firstResponseAtStr;
    private String slaResponseDueAtStr;
    private String slaResolveDueAtStr;
}