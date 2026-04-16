// package com.example.tckt.demo.controller;

// import com.example.tckt.demo.dto.UserDTO;
// import com.example.tckt.demo.service.OtpService;

// import com.example.tckt.demo.service.UserService;
// import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// @Slf4j
// @Controller
// @RequestMapping("/verify")
// @RequiredArgsConstructor
// public class OtpVerificationController {

//     private final otpService otpService;
//     private final UserService userService;

//     // ─────────────────────────────────────────────
//     // Show OTP verification page
//     // ─────────────────────────────────────────────

//     @GetMapping
//     public String showVerifyPage(HttpSession session, Model model,
//                                   RedirectAttributes ra) {
//         // If no pending registration in session, redirect to register
//         UserDTO pendingUser = (UserDTO) session.getAttribute("pendingUser");
//         if (pendingUser == null) {
//             ra.addFlashAttribute("error", "Session expired. Please register again.");
//             return "redirect:/register";
//         }

//         model.addAttribute("email", maskEmail(pendingUser.getEmail()));
//         model.addAttribute("canResend", otpService.canResend(pendingUser.getEmail()));
//         return "auth/otp-verify";
//     }

//     // ─────────────────────────────────────────────
//     // Verify OTP submitted by user
//     // ─────────────────────────────────────────────

//     @PostMapping("/submit")
//     public String submitOtp(@RequestParam String otp,
//                              HttpSession session,
//                              RedirectAttributes ra) {

//         UserDTO pendingUser = (UserDTO) session.getAttribute("pendingUser");
//         if (pendingUser == null) {
//             ra.addFlashAttribute("error", "Session expired. Please register again.");
//             return "redirect:/register";
//         }

//         OtpResult result = otpService.verifyOtp(pendingUser.getEmail(), otp);

//         switch (result) {
//             case SUCCESS -> {
//                 try {
//                     // OTP verified — now actually create the account
//                     userService.createUser(pendingUser);
//                     session.removeAttribute("pendingUser");
//                     log.info("Account created after OTP verification: {}",
//                             pendingUser.getEmail());
//                     ra.addFlashAttribute("success",
//                             "Account created successfully! Please log in.");
//                     return "redirect:/login?success";
//                 } catch (Exception e) {
//                     log.error("Account creation failed after OTP: {}", e.getMessage());
//                     ra.addFlashAttribute("error",
//                             "Account creation failed: " + e.getMessage());
//                     return "redirect:/register";
//                 }
//             }
//             case INVALID -> {
//                 ra.addFlashAttribute("error",
//                         "Incorrect OTP. Please try again.");
//                 return "redirect:/verify";
//             }
//             case EXPIRED -> {
//                 ra.addFlashAttribute("error",
//                         "OTP has expired. Please request a new one.");
//                 return "redirect:/verify";
//             }
//             case MAX_ATTEMPTS -> {
//                 session.removeAttribute("pendingUser");
//                 ra.addFlashAttribute("error",
//                         "Too many incorrect attempts. Please register again.");
//                 return "redirect:/register";
//             }
//             default -> {
//                 ra.addFlashAttribute("error",
//                         "OTP not found. Please request a new one.");
//                 return "redirect:/verify";
//             }
//         }
//     }

//     // ─────────────────────────────────────────────
//     // Resend OTP
//     // ─────────────────────────────────────────────

//     @PostMapping("/resend")
//     public String resendOtp(HttpSession session, RedirectAttributes ra) {
//         UserDTO pendingUser = (UserDTO) session.getAttribute("pendingUser");
//         if (pendingUser == null) {
//             ra.addFlashAttribute("error", "Session expired. Please register again.");
//             return "redirect:/register";
//         }

//         try {
//             otpService.generateAndSendOtp(
//                     pendingUser.getEmail(), pendingUser.getFirstName());
//             ra.addFlashAttribute("success",
//                     "A new OTP has been sent to your email.");
//         } catch (Exception e) {
//             ra.addFlashAttribute("error",
//                     "Failed to resend OTP: " + e.getMessage());
//         }

//         return "redirect:/verify";
//     }

//     // ─────────────────────────────────────────────
//     // Helper — mask email for display
//     // ─────────────────────────────────────────────

//     private String maskEmail(String email) {
//         int atIndex = email.indexOf('@');
//         if (atIndex <= 2) return email;
//         String name = email.substring(0, atIndex);
//         String domain = email.substring(atIndex);
//         String masked = name.substring(0, 2)
//                 + "*".repeat(name.length() - 2);
//         return masked + domain;
//     }
// }