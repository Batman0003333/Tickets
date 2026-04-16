package com.example.tckt.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Boolean isInternal;
    private Long ticketId;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}