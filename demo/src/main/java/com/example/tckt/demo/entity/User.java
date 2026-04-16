package com.example.tckt.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;

    private String designation;        // e.g. "Software Engineer", "IT Manager"

    private String employeeId;         // company employee ID for internal users

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;         // INTERNAL (employee) or EXTERNAL (customer)

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;  // useful for admin dashboard + security audits

    // ─────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────

    public enum UserRole {
        USER, ADMIN, AGENT           // AGENT replaces SUPPORT — matches your security setup
    }

    public enum UserType {
        INTERNAL,                    // company employee raising IT tickets
        EXTERNAL                     // external customer / vendor
    }

    // ─────────────────────────────────────────────
    // Convenience method
    // ─────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }
}