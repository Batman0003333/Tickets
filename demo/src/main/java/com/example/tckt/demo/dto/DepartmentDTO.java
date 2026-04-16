package com.example.tckt.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
    private Long userCount;
    private Long ticketCount;
}