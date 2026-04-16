package com.example.tckt.demo.controller;

import com.example.tckt.demo.dto.DepartmentDTO;
import com.example.tckt.demo.dto.TicketDTO;
import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.entity.Ticket;
import com.example.tckt.demo.entity.User;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.service.CommentService;
import com.example.tckt.demo.service.DepartmentService;
import com.example.tckt.demo.service.TicketService;
import com.example.tckt.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final TicketService ticketService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final CommentService commentService;

    // ─────────────────────────────────────────────
    // Dashboard
    // ─────────────────────────────────────────────

   @GetMapping("/dashboard")
public String adminDashboard(Model model) {
    model.addAttribute("totalTickets",    ticketService.countAllTickets());
    model.addAttribute("openTickets",     ticketService.countByStatus(Ticket.TicketStatus.OPEN));
    model.addAttribute("inProgress",      ticketService.countByStatus(Ticket.TicketStatus.IN_PROGRESS));
    model.addAttribute("resolvedTickets", ticketService.countByStatus(Ticket.TicketStatus.RESOLVED));
    model.addAttribute("activeUsers",     userService.countActiveUsers());
    model.addAttribute("recentTickets",
            ticketService.getAllTickets(
                    PageRequest.of(0, 5, Sort.by("createdAt").descending())
            ).getContent());
    return "admin/dashboard";
}

    // ─────────────────────────────────────────────
    // Tickets
    // ─────────────────────────────────────────────

    @GetMapping("/all-tickets")
    public String showAllTickets(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String status,
                                 Model model) {

        Page<TicketDTO> tickets;

        if (status != null && !status.isBlank()) {
            Ticket.TicketStatus ticketStatus = Ticket.TicketStatus.valueOf(status.toUpperCase());
            tickets = ticketService.getTicketsByStatus(ticketStatus,
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            tickets = ticketService.getAllTickets(
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());
        model.addAttribute("selectedStatus", status);

        return "admin/all-tickets";
    }

   @GetMapping("/ticket/{id}")
public String viewTicketForAdmin(@PathVariable Long id, Model model) {
    TicketDTO ticket = ticketService.getTicketById(id)
            .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));

    model.addAttribute("ticket", ticket);
    model.addAttribute("comments", commentService.getAllCommentsByTicket(id));
    model.addAttribute("allAgents", userService.getUsersByRole(User.UserRole.AGENT));

    return "admin/ticket-details";
}

    @PostMapping("/ticket/process")
    public String handleDecision(@RequestParam Long ticketId,
                                 @RequestParam String action,
                                 @RequestParam(required = false) String comments,
                                 @RequestParam(required = false) Long nextAuthorityId,
                                 Principal principal,
                                 RedirectAttributes ra) {
        try {
            ticketService.processTicket(ticketId, action, comments,
                    nextAuthorityId, principal.getName());
            ra.addFlashAttribute("success",
                    "Ticket #" + ticketId + " processed with action: " + action);
            log.info("Admin [{}] processed ticket [{}] — action: {}",
                    principal.getName(), ticketId, action);
        } catch (Exception e) {
            log.error("Failed to process ticket [{}] — {}", ticketId, e.getMessage());
            ra.addFlashAttribute("error", "Failed to process ticket: " + e.getMessage());
        }
        return "redirect:/admin/all-tickets";
    }

    // ─────────────────────────────────────────────
    // Users
    // ─────────────────────────────────────────────

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addNewUser(@ModelAttribute UserDTO userDTO, RedirectAttributes ra) {
        try {
            userService.createUser(userDTO);
            ra.addFlashAttribute("success",
                    "Employee " + userDTO.getFirstName() + " registered successfully.");
            log.info("Admin created new user: {}", userDTO.getEmail());
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            ra.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id,
                                   Principal principal,
                                   RedirectAttributes ra) {
        try {
            userService.toggleUserActive(id);
            ra.addFlashAttribute("success", "User status updated.");
            log.info("Admin [{}] toggled active status for user [{}]",
                    principal.getName(), id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Update user ──────────────────────────────────
// @PostMapping("/users/{id}/update")
// public String updateUser(@PathVariable Long id,
//                          @ModelAttribute UserDTO userDTO,
//                          RedirectAttributes ra) {
//     try {
//         userService.updateUserById(id, userDTO);
//         ra.addFlashAttribute("success", "User updated successfully.");
//     } catch (Exception e) {
//         ra.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
//     }
//     return "redirect:/admin/users";
// }

// ── Delete user ──────────────────────────────────
// @PostMapping("/users/{id}/delete")
// public String deleteUser(@PathVariable Long id,
//                          Principal principal,
//                          RedirectAttributes ra) {
//     try {
//         userService.deleteUser(id);
//         log.info("Admin [{}] deleted user [{}]", principal.getName(), id);
//         ra.addFlashAttribute("success", "User deleted successfully.");
//     } catch (Exception e) {
//         ra.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
//     }
//     return "redirect:/admin/users";
// }

    // ─────────────────────────────────────────────
    // Departments
    // ─────────────────────────────────────────────

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/departments";
    }

    @PostMapping("/departments/add")
    public String addDepartment(@ModelAttribute DepartmentDTO deptDTO, RedirectAttributes ra) {
        try {
            departmentService.createDepartment(deptDTO);
            ra.addFlashAttribute("success",
                    "Department '" + deptDTO.getName() + "' added successfully.");
        } catch (Exception e) {
            log.error("Failed to create department: {}", e.getMessage());
            ra.addFlashAttribute("error", "Could not add department: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ─────────────────────────────────────────────
    // Reports
    // ─────────────────────────────────────────────

    @GetMapping("/reports")
    public String showReports(Model model) {
        model.addAttribute("totalTickets",    ticketService.countAllTickets());
        model.addAttribute("openTickets",     ticketService.countByStatus(Ticket.TicketStatus.OPEN));
        model.addAttribute("resolvedTickets", ticketService.countByStatus(Ticket.TicketStatus.RESOLVED));
        model.addAttribute("inProgress",      ticketService.countByStatus(Ticket.TicketStatus.IN_PROGRESS));
        model.addAttribute("departments",     departmentService.getAllDepartments());
        return "admin/reports";
    }

    // ─────────────────────────────────────────────
    // Settings
    // ─────────────────────────────────────────────

    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("totalUsers",  userService.countActiveUsers());
        return "admin/settings";
    }
}