package com.example.tckt.demo.config;

import com.example.tckt.demo.entity.Department;
import com.example.tckt.demo.entity.User;
import com.example.tckt.demo.repository.DepartmentRepository;
import com.example.tckt.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // ─────────────────────────────────────────────
        // Seed departments
        // ─────────────────────────────────────────────
        if (departmentRepository.count() == 0) {

            Department it = new Department();
            it.setName("IT Support");
            it.setCode("ITS");
            it.setDescription("General IT support and helpdesk");
            it.setIsActive(true);
            departmentRepository.save(it);

            Department network = new Department();
            network.setName("Network & Infrastructure");
            network.setCode("NET");
            network.setDescription("VPN, servers, hardware, connectivity");
            network.setIsActive(true);
            departmentRepository.save(network);

            Department software = new Department();
            software.setName("Software & Applications");
            software.setCode("SW");
            software.setDescription("Installations, licensing, bugs");
            software.setIsActive(true);
            departmentRepository.save(software);

            Department security = new Department();
            security.setName("Security & Access");
            security.setCode("SEC");
            security.setDescription("Passwords, permissions, MFA");
            security.setIsActive(true);
            departmentRepository.save(security);

            log.info(">>> Departments seeded successfully");
        }

        // ─────────────────────────────────────────────
        // Seed admin user
        // ─────────────────────────────────────────────
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {

            Department itDept = departmentRepository.findAll().get(0);

            User admin = new User();
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setPhoneNumber("1234567890");
            admin.setRole(User.UserRole.ADMIN);
            admin.setUserType(User.UserType.INTERNAL);
            admin.setDepartment(itDept);
            admin.setIsActive(true);

            userRepository.save(admin);
            log.info(">>> Admin account created — email: admin@test.com, password: admin123");
        }

        // ─────────────────────────────────────────────
        // Seed a test regular user
        // ─────────────────────────────────────────────
        if (userRepository.findByEmail("user@test.com").isEmpty()) {

            Department itDept = departmentRepository.findAll().get(0);

            User user = new User();
            user.setEmail("user@test.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFirstName("Test");
            user.setLastName("User");
            user.setPhoneNumber("9876543210");
            user.setRole(User.UserRole.USER);
            user.setUserType(User.UserType.INTERNAL);
            user.setDepartment(itDept);
            user.setIsActive(true);

            userRepository.save(user);
            log.info(">>> Test user created — email: user@test.com, password: user123");
        }
    }
}