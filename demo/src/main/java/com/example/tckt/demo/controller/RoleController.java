package com.example.tckt.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Set;

@Controller
public class RoleController {

   @GetMapping("/role")
public String redirectByRole(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return "redirect:/login";
    }

    Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

    // 1. Check for Admin
    if (roles.contains("ROLE_ADMIN")) {
        return "redirect:/admin/dashboard";
    } 
    
    // 2. Check for User
    if (roles.contains("ROLE_USER")) {
        // If they tried to access an admin page, Spring Security might have 
        // sent them here. Redirect them to their own dashboard.
        return "redirect:/user/dashboard";
    }

    return "redirect:/login?error=unauthorized";
}
}