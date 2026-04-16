package com.example.tckt.demo.repository;

import com.example.tckt.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 1. Used for the Admin Audit Trail (Sorted by newest first)
    List<Comment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    // 2. Fixes the error in CommentService.getCommentsByTicket
    List<Comment> findByTicketId(Long ticketId);

    // 3. Fixes the error in CommentService.getCommentsByUser
    // Note: This must match the field name 'createdBy' in your Comment entity
    List<Comment> findByCreatedById(Long userId);

    // Add to CommentRepository.java:

}