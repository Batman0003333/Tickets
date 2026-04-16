package com.example.tckt.demo.controller;

import com.example.tckt.demo.dto.CommentDTO;
import com.example.tckt.demo.dto.TicketDTO;
import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.service.CommentService;
import com.example.tckt.demo.service.DepartmentService;
import com.example.tckt.demo.service.TicketService;
import com.example.tckt.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")        // whole controller requires USER role
public class UserTicketController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final UserService userService;
private final DepartmentService departmentService;
    // ─────────────────────────────────────────────
    // Dashboard
    // ─────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String userDashboard(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model, Principal principal) {

        UserDTO user = userService.getUserByEmail(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        model.addAttribute("user", user);
        model.addAttribute("tickets", ticketService.getTicketsByUser(user.getId(), pageable));
        model.addAttribute("totalCount", ticketService.countByUserId(user.getId()));
        model.addAttribute("openCount",
                ticketService.countByUserIdAndStatus(user.getId(), "OPEN"));
        model.addAttribute("resolvedCount",
                ticketService.countByUserIdAndStatus(user.getId(), "RESOLVED"));

        return "user/dashboard";
    }

    // ─────────────────────────────────────────────
    // Create ticket
    // ─────────────────────────────────────────────

    @GetMapping("/create-ticket")
public String createTicketForm(Model model) {
    model.addAttribute("ticketDTO", new TicketDTO());
    model.addAttribute("departments", departmentService.getActiveDepartments());  // ← add this
    return "user/create-ticket";
}

@PostMapping("/create-ticket")
public String createTicket(@ModelAttribute TicketDTO ticketDTO,
                           Principal principal,
                           RedirectAttributes ra) {
    try {
        UserDTO user = userService.getUserByEmail(principal.getName());
        ticketDTO.setCreatedById(user.getId());
        TicketDTO created = ticketService.createTicket(ticketDTO);
        ra.addFlashAttribute("success",
                "Ticket " + created.getTicketNumber() + " submitted successfully.");
        log.info("Ticket created: {} by user: {}", created.getTicketNumber(), principal.getName());
    } catch (Exception e) {
        log.error("Ticket creation failed for user: {}", principal.getName(), e);
        ra.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
    }
    return "redirect:/user/my-tickets";   // ← redirects to my-tickets which now shows data
}

    // ─────────────────────────────────────────────
    // View tickets
    // ─────────────────────────────────────────────

    @GetMapping("/my-tickets")
    public String myTickets(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Principal principal, Model model) {

        UserDTO user = userService.getUserByEmail(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TicketDTO> tickets = ticketService.getTicketsByUser(user.getId(), pageable);
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tickets.getTotalPages());

        return "user/my-tickets";
    }

    @GetMapping("/ticket/{id}")
    public String viewTicket(@PathVariable Long id,
                             Principal principal,
                             Model model) {

        TicketDTO ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));

        // Security check — user can only view their own tickets
        UserDTO user = userService.getUserByEmail(principal.getName());
        if (!ticket.getCreatedById().equals(user.getId())) {
            log.warn("User [{}] attempted to view ticket [{}] they do not own", 
                     principal.getName(), id);
            return "redirect:/user/my-tickets?error=access-denied";
        }

        List<CommentDTO> comments = commentService.getCommentsByTicket(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);

        return "user/ticket-details";
    }

    // ─────────────────────────────────────────────
    // Profile
    // ─────────────────────────────────────────────

    @GetMapping("/profile")
    public String userProfile(Principal principal, Model model) {
        UserDTO user = userService.getUserByEmail(principal.getName());
        long count = (user != null && user.getId() != null)
                ? ticketService.countByUserId(user.getId()) : 0;

        model.addAttribute("user", user);
        model.addAttribute("totalCount", count);

        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserDTO userDTO,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            userService.updateUserProfile(principal.getName(), userDTO);
            ra.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            log.error("Profile update failed for user: {}", principal.getName(), e);
            ra.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }
}