
package com.example.tckt.demo.repository;

import com.example.tckt.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Finds any User (or Admin) with this phone number
    List<User> findByPhoneNumber(String phoneNumber);

    // Finds any User (or Admin) in a specific department
    // 'departmentId' works because Spring JPA looks inside the 'department' 
    // object for the 'id' field automatically.
    List<User> findByDepartmentId(Long departmentId);
    
    // Highly recommended: for login/validation
    Optional<User> findByEmail(String email);

    List<User> findByRole(User.UserRole role);
    long countByIsActiveTrue();
    long countByDepartmentId(Long departmentId);
}