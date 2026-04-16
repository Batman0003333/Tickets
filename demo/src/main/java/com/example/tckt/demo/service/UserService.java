package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.entity.Department;
import com.example.tckt.demo.entity.User;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.repository.DepartmentRepository;
import com.example.tckt.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)    // ← add
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────

    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    public List<UserDTO> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }

    // ─────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Block duplicate emails
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use: " + userDTO.getEmail());
        });

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setDesignation(userDTO.getDesignation());
        user.setEmployeeId(userDTO.getEmployeeId());
        user.setIsActive(true);

        // Default role to USER if not specified
        // FIXED — getRole() returns UserRole enum directly, no valueOf needed
user.setRole(userDTO.getRole() != null
        ? userDTO.getRole()
        : User.UserRole.USER);

        // Default userType to INTERNAL if not specified
        user.setUserType(userDTO.getUserType() != null
                ? User.UserType.valueOf(userDTO.getUserType())
                : User.UserType.INTERNAL);

        // Encode password — use default if none provided
        String rawPassword = (userDTO.getPassword() != null && !userDTO.getPassword().isBlank())
                ? userDTO.getPassword()
                : "Change@Me123";              // force change on first login (add flag later)
        user.setPassword(passwordEncoder.encode(rawPassword));

        // Assign department
        if (userDTO.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Department not found: " + userDTO.getDepartmentId()));
            user.setDepartment(dept);
        }

        log.info("Creating new user: {} with role: {}", user.getEmail(), user.getRole());
        return convertToDTO(userRepository.save(user));
    }

    // ─────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────

    @Transactional
    public void updateUserProfile(String email, UserDTO userDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (userDTO.getFirstName() != null)   user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null)    user.setLastName(userDTO.getLastName());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getDesignation() != null) user.setDesignation(userDTO.getDesignation());

        // Only update password if explicitly provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            log.info("Password updated for user: {}", email);
        }

        userRepository.save(user);
        log.info("Profile updated for user: {}", email);
    }

    @Transactional
    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        log.info("User [{}] active status set to: {}", user.getEmail(), user.getIsActive());
    }

    @Transactional
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
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
        dto.setDesignation(user.getDesignation());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setIsActive(user.getIsActive());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        if (user.getUserType() != null) {
            dto.setUserType(user.getUserType().name());
        }
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }

        return dto;
    }

    @Transactional
public void updateUserById(Long id, UserDTO userDTO) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

    if (userDTO.getFirstName() != null)   user.setFirstName(userDTO.getFirstName());
    if (userDTO.getLastName() != null)    user.setLastName(userDTO.getLastName());
    if (userDTO.getEmail() != null)       user.setEmail(userDTO.getEmail());
    if (userDTO.getRole() != null)        user.setRole(userDTO.getRole());

    // Only update password if a new one was provided
    if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    }

    // Update department
    if (userDTO.getDepartmentId() != null) {
        departmentRepository.findById(userDTO.getDepartmentId())
                .ifPresent(user::setDepartment);
    }

    userRepository.save(user);
    log.info("User [{}] updated by admin", id);
}


// public void checkEmailAvailable(String email) {
//     userRepository.findByEmail(email).ifPresent(u -> {
//         throw new IllegalArgumentException(
//                 "Email already in use: " + email);
//     });
// }
// @Transactional
// public void deleteUser(Long id) {
//     User user = userRepository.findById(id)
//             .orElseThrow(() -> new RuntimeException("User not found: " + id));
//     userRepository.delete(user);
//     log.info("User [{}] deleted", id);
// }
}