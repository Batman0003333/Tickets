package com.example.tckt.demo.controller;

import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.service.DepartmentService;
import com.example.tckt.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final DepartmentService departmentService;

    // ─────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // ─────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("departments", departmentService.getActiveDepartments());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDTO dto,
                               Model model,
                               RedirectAttributes ra) {
        try {
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                model.addAttribute("error", "Password is required.");
                model.addAttribute("departments", departmentService.getActiveDepartments());
                return "auth/register";
            }

            dto.setRole("USER");
            dto.setUserType("INTERNAL");
            userService.createUser(dto);

            log.info("New user registered: {}", dto.getEmail());
            ra.addFlashAttribute("success", "Account created successfully. Please log in.");
            return "redirect:/login?success";

        } catch (Exception e) {
            log.error("Registration failed for email {}: {}", dto.getEmail(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getActiveDepartments());
            return "auth/register";
        }
    }

    
    
}