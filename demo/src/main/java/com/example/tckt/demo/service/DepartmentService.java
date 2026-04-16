package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.DepartmentDTO;
import com.example.tckt.demo.entity.Department;
import com.example.tckt.demo.repository.DepartmentRepository;
import com.example.tckt.demo.repository.TicketRepository;
import com.example.tckt.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)    // ← add
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TicketRepository ticketRepository;

    private final UserRepository userRepository;
    // ─────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO deptDTO) {
        // Block duplicate names
        departmentRepository.findByName(deptDTO.getName()).ifPresent(d -> {
            throw new IllegalArgumentException(
                    "Department already exists: " + deptDTO.getName());
        });

        Department dept = new Department();
        dept.setName(deptDTO.getName());
        dept.setCode(deptDTO.getCode() != null
                ? deptDTO.getCode().toUpperCase()
                : deptDTO.getName().substring(0, Math.min(4, deptDTO.getName().length()))
                         .toUpperCase());
        dept.setDescription(deptDTO.getDescription());
        dept.setIsActive(true);

        log.info("Department created: {} [{}]", dept.getName(), dept.getCode());
        return convertToDTO(departmentRepository.save(dept));
    }

    // ─────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────

    public Optional<DepartmentDTO> getDepartmentById(Long id) {
        return departmentRepository.findById(id).map(this::convertToDTO);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────

    @Transactional
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO deptDTO) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

        if (deptDTO.getName() != null)        dept.setName(deptDTO.getName());
        if (deptDTO.getDescription() != null) dept.setDescription(deptDTO.getDescription());
        if (deptDTO.getCode() != null)        dept.setCode(deptDTO.getCode().toUpperCase());

        log.info("Department updated: {}", dept.getName());
        return convertToDTO(departmentRepository.save(dept));
    }

    @Transactional
    public void toggleDepartmentActive(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
        dept.setIsActive(!dept.getIsActive());
        departmentRepository.save(dept);
        log.info("Department [{}] active status set to: {}", dept.getName(), dept.getIsActive());
    }

    // ─────────────────────────────────────────────
    // Converter
    // ─────────────────────────────────────────────

  private DepartmentDTO convertToDTO(Department department) {
    DepartmentDTO dto = new DepartmentDTO();
    dto.setId(department.getId());
    dto.setName(department.getName());
    dto.setCode(department.getCode());
    dto.setDescription(department.getDescription());
    dto.setIsActive(department.getIsActive());
    dto.setUserCount(userRepository.countByDepartmentId(department.getId()));
    dto.setTicketCount(ticketRepository.countByDepartmentId(department.getId()));
    return dto;
}
}