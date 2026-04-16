package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.UserDTO;
import com.example.tckt.demo.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void createUser_shouldPersist() {
        UserDTO dto = new UserDTO();
        dto.setEmail("testuser@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setPassword("password123");
        dto.setRole(User.UserRole.USER);

        UserDTO result = userService.createUser(dto);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId(), "saved user should have generated id");
        Assertions.assertEquals("testuser@example.com", result.getEmail());
    }

    @Test
    public void createUser_duplicateEmail_throws() {
        UserDTO dto = new UserDTO();
        dto.setEmail("dup@example.com");
        dto.setFirstName("Dup");
        dto.setLastName("User");
        dto.setPhoneNumber("9876543210");
        dto.setPassword("pass");
        dto.setRole(User.UserRole.USER);
        userService.createUser(dto);

        UserDTO dto2 = new UserDTO();
        dto2.setEmail("dup@example.com");
        dto2.setFirstName("Second");
        dto2.setLastName("User");
        dto2.setPhoneNumber("000");
        dto2.setPassword("pass2");
        dto2.setRole(User.UserRole.USER);

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto2));
    }
}