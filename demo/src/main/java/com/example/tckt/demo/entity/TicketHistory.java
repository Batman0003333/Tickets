package com.example.tckt.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(nullable = false)
    private String fieldChanged;       // e.g. "status", "assignedTo", "priority"

    private String oldValue;           // e.g. "OPEN"
    private String newValue;           // e.g. "IN_PROGRESS"

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime changedAt;
}