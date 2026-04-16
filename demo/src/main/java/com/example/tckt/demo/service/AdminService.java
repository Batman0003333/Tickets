package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.entity.Department;
import com.example.tckt.demo.entity.Ticket;
import com.example.tckt.demo.entity.User;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.repository.DepartmentRepository;
import com.example.tckt.demo.repository.TicketRepository;
import com.example.tckt.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)    // ← add
public class AdminService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // Ticket management
    // ─────────────────────────────────────────────

    @Transactional
    public void updateTicketStatusAndDepartment(Long ticketId, String status, Long departmentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        try {
            ticket.setStatus(Ticket.TicketStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        ticket.setDepartment(dept);
        ticketRepository.save(ticket);
        log.info("Ticket [{}] updated — status: {}, department: {}", ticketId, status, dept.getName());
    }

    // ─────────────────────────────────────────────
    // Admin user management
    // ─────────────────────────────────────────────

    @Transactional
    public UserDTO createAdmin(UserDTO userDTO) {
        // Block duplicate emails
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use: " + userDTO.getEmail());
        });

        User admin = new User();
        admin.setEmail(userDTO.getEmail());
        admin.setFirstName(userDTO.getFirstName());
        admin.setLastName(userDTO.getLastName());
        admin.setPhoneNumber(userDTO.getPhoneNumber());
        admin.setRole(User.UserRole.ADMIN);
        admin.setUserType(User.UserType.INTERNAL);
        admin.setIsActive(true);

        String rawPassword = (userDTO.getPassword() != null && !userDTO.getPassword().isBlank())
                ? userDTO.getPassword()
                : "Admin@Change123";
        admin.setPassword(passwordEncoder.encode(rawPassword));

        if (userDTO.getDepartmentId() != null) {
            departmentRepository.findById(userDTO.getDepartmentId())
                    .ifPresent(admin::setDepartment);
        }

        log.info("New admin created: {}", admin.getEmail());
        return convertToDTO(userRepository.save(admin));
    }

    public List<UserDTO> getAllAdmins() {
        return userRepository.findByRole(User.UserRole.ADMIN)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }

    // ─────────────────────────────────────────────
    // Password management
    // ─────────────────────────────────────────────

    @Transactional
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        return userRepository.findById(userId).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                log.warn("Password update failed for user [{}] — old password mismatch", userId);
                return false;
            }
            if (newPassword.length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password updated for user [{}]", userId);
            return true;
        }).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    // ─────────────────────────────────────────────
    // User activation
    // ─────────────────────────────────────────────

    @Transactional
    public UserDTO deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setIsActive(false);
        log.info("User [{}] deactivated", userId);
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setIsActive(true);
        log.info("User [{}] reactivated", userId);
        return convertToDTO(userRepository.save(user));
    }

    // ─────────────────────────────────────────────
    // Converter
    // ─────────────────────────────────────────────

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        if (user.getRole() != null)       dto.setRole(user.getRole().name());
        if (user.getUserType() != null)   dto.setUserType(user.getUserType().name());
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        return dto;
    }
}