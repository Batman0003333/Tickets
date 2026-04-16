package com.example.tckt.demo.controller;

import com.example.tckt.demo.service.CommentService;
import com.example.tckt.demo.dto.CommentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Add comment
    @PostMapping("/add")
    public CommentDTO addComment(@RequestBody CommentDTO commentDTO) {
        return commentService.addComment(commentDTO);
    }

    // Get comments by ticket
    @GetMapping("/ticket/{ticketId}")
    public List<CommentDTO> getCommentsByTicket(@PathVariable Long ticketId) {
        return commentService.getCommentsByTicket(ticketId);
    }

    // Delete comment
    @DeleteMapping("/{id}")
    public boolean deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }
}
