package com.example.tckt.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;               // e.g. "NET", "SEC", "SW" — for routing rules

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<User> users;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<Ticket> tickets;
}