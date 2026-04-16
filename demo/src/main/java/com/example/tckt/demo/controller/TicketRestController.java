package com.example.tckt.demo.controller;

import com.example.tckt.demo.service.TicketService;
import com.example.tckt.demo.dto.TicketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketRestController {

    @Autowired
    private TicketService ticketService;

    // Get all tickets with pagination
    @GetMapping
    public ResponseEntity<Page<TicketDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ticketService.getAllTickets(pageable));
    }

    // Get ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new ticket
    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.createTicket(ticketDTO));
    }

    // Update ticket status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            TicketDTO updated = ticketService.updateTicketStatus(id, 
                    com.example.tckt.demo.entity.Ticket.TicketStatus.valueOf(status));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    // Assign ticket to user
    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketDTO> assignTicket(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(ticketService.assignTicket(id, userId));
    }

    // Delete ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        if (ticketService.deleteTicket(id)) {
            return ResponseEntity.ok().body("{\"message\":\"Ticket deleted successfully\"}");
        }
        return ResponseEntity.notFound().build();
    }
}
