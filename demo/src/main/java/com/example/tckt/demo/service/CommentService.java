package com.example.tckt.demo.service;

import com.example.tckt.demo.dto.CommentDTO;
import com.example.tckt.demo.entity.Comment;
import com.example.tckt.demo.exception.TicketNotFoundException;
import com.example.tckt.demo.repository.CommentRepository;
import com.example.tckt.demo.repository.TicketRepository;
import com.example.tckt.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)    // ← add this
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    // ─────────────────────────────────────────────
    // Add comment
    // ─────────────────────────────────────────────

    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setIsInternal(
                commentDTO.getIsInternal() != null && commentDTO.getIsInternal());

        comment.setTicket(
                ticketRepository.findById(commentDTO.getTicketId())
                        .orElseThrow(() -> new TicketNotFoundException(
                                "Ticket not found: " + commentDTO.getTicketId())));

        comment.setCreatedBy(
                userRepository.findById(commentDTO.getCreatedById())
                        .orElseThrow(() -> new RuntimeException(
                                "User not found: " + commentDTO.getCreatedById())));

        comment.setCreatedAt(LocalDateTime.now(IST_ZONE));

        log.info("Comment added to ticket [{}] by user [{}] — internal: {}",
                commentDTO.getTicketId(), commentDTO.getCreatedById(),
                comment.getIsInternal());

        return convertToDTO(commentRepository.save(comment));
    }

    // ─────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────

    // Returns only public comments — for the ticket submitter view
    public List<CommentDTO> getCommentsByTicket(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsInternal()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Returns ALL comments including internal — for agent/admin view
    public List<CommentDTO> getAllCommentsByTicket(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CommentDTO> getCommentsByUser(Long userId) {
        return commentRepository.findByCreatedById(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────

    @Transactional
    public boolean deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment not found: " + commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Comment [{}] deleted", commentId);
        return true;
    }

    // ─────────────────────────────────────────────
    // Converter
    // ─────────────────────────────────────────────

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setIsInternal(comment.getIsInternal());
        dto.setTicketId(comment.getTicket().getId());
        dto.setCreatedById(comment.getCreatedBy().getId());
        dto.setCreatedByName(comment.getCreatedBy().getFirstName()
                + " " + comment.getCreatedBy().getLastName());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}