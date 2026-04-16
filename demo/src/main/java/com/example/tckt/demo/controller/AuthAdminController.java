package com.example.tckt.demo.controller;

import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.service.AdminService;
import com.example.tckt.demo.service.DepartmentService;
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
public class AuthAdminController {

    private final AdminService adminService;
    private final DepartmentService departmentService;

    // ─────────────────────────────────────────────
    // Admin login page
    // ─────────────────────────────────────────────

    @GetMapping("/admin/login")
    public String showAdminLoginForm() {
        return "auth/admin-login";
    }

    // ─────────────────────────────────────────────
    // Admin registration
    // ─────────────────────────────────────────────

    @GetMapping("/admin/registerAdmin")
    public String showAdminRegistrationForm(Model model) {
        model.addAttribute("admin", new UserDTO());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "auth/registerAdmin";
    }

    @PostMapping("/admin/registerAdmin")
    public String registerAdmin(@ModelAttribute("admin") UserDTO adminDTO,
                                Model model,
                                RedirectAttributes ra) {
        try {
            adminDTO.setRole("ADMIN");
            adminService.createAdmin(adminDTO);
            log.info("New admin registered: {}", adminDTO.getEmail());
            ra.addFlashAttribute("success", "Admin account created. Please log in.");
            return "redirect:/admin/login?success";
        } catch (Exception e) {
            log.error("Admin registration failed: {}", e.getMessage());
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "auth/registerAdmin";
        }
    }
}