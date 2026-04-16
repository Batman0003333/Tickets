package com.example.tckt.demo.config;

import com.example.tckt.demo.security.CustomAuthenticationSuccessHandler;
import com.example.tckt.demo.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers
@RequiredArgsConstructor       // replaces both @Autowired usages
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    // ─────────────────────────────────────────────
    // Beans
    // ─────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // strength 12 — production standard
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ─────────────────────────────────────────────
    // Security filter chain
    // ─────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF ──────────────────────────────────────────────────────
            // Keep CSRF enabled for MVC (Thymeleaf adds the token automatically).
            // Only disable for pure REST APIs. Since you have TicketRestController,
            // use the matcher below to disable CSRF only for /api/** routes.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )

            // ── AUTHENTICATION PROVIDER ───────────────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── URL AUTHORIZATION ─────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Public pages — no login needed
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/verify",          // ← add this
                    "/verify/submit",   // ← add thi
                    "/verify/resend",
                    "/admin/login",
                    "/admin/registerAdmin"
                ).permitAll()

                // Static assets — always public
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                // Admin routes — ADMIN role only
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // User routes — USER role only
                .requestMatchers("/user/**").hasRole("USER")

                // REST API — authenticated, fine-grained via @PreAuthorize
                .requestMatchers("/api/**").authenticated()

                // Roles page — any authenticated user
                .requestMatchers("/role").authenticated()

                // Everything else — must be logged in
                .anyRequest().authenticated()
            )

            // ── FORM LOGIN ────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")   // redirect on bad credentials
                .permitAll()
            )

            // ── LOGOUT ────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)          // explicitly clear auth context
                .permitAll()
            )

            // ── SESSION MANAGEMENT ────────────────────────────────────────
            .sessionManagement(session -> session
                .maximumSessions(1)                 // prevent same user logging in twice
                .maxSessionsPreventsLogin(false)    // new login kicks out old session
            )

            // ── SECURITY HEADERS ──────────────────────────────────────────
            .headers(headers -> headers
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentSecurityPolicy(csp -> csp
    .policyDirectives("default-src 'self'; " +
                      "script-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                      "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net fonts.googleapis.com; " +
                      "font-src 'self' fonts.gstatic.com cdn.jsdelivr.net; " +
                      "img-src 'self' data: cdn.jsdelivr.net;")
)
                .frameOptions(frame -> frame.sameOrigin()) // allows same-origin iframes
            );

        return http.build();
    }
}