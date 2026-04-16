package com.example.tckt.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String email = authentication.getName();
        String referer = request.getHeader("Referer");

        log.info("Login success — user: {}, roles: {}, referer: {}", email, roles, referer);

        

        // 1. Non-admin tried to log in via /admin/login — block them
        if (referer != null && referer.contains("/admin/login") && !roles.contains("ROLE_ADMIN")) {
            log.warn("Non-admin user [{}] attempted admin login — redirecting", email);
            response.sendRedirect("/admin/login?error=access-denied");
            return;
        }

        // 2. Admin logged in via the regular /login page — send to admin dashboard
        if (referer != null
                && referer.contains("/login")
                && !referer.contains("/admin/login")
                && roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        // 3. Normal role-based redirect
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_AGENT")) {
            response.sendRedirect("/agent/dashboard");   // ready for when you add agents
        } else {
            response.sendRedirect("/user/dashboard");
        }
    }
}