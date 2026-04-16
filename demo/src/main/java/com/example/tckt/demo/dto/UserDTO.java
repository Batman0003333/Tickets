package com.example.tckt.demo.dto;

import com.example.tckt.demo.entity.User.UserRole;
import java.time.LocalDateTime;

public class UserDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String designation;
    private String employeeId;
    private String password;
    private UserRole role;
    private String userType;
    private Boolean isActive;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────

    public UserDTO() {}

    // ─────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(String role) {
        if (role != null) {
            this.role = UserRole.valueOf(role.toUpperCase());
        }
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}