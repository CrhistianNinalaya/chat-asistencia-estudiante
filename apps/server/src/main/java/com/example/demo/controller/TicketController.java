package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TicketDto;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/tickets"})
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<TicketDto.Response> listAll(
            @RequestParam(value = "status", required = false) TicketStatus status,
            @RequestParam(value = "priority", required = false) TicketPriority priority,
            @AuthenticationPrincipal UserPrincipal user) {
        return ticketService.listTickets(status, priority, user);
    }

    @PostMapping
    public ResponseEntity<TicketDto.Response> create(
            @Valid @RequestBody TicketDto.CreateRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        TicketDto.Response response = ticketService.createTicket(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{ticketId}/status")
    public TicketDto.Response updateStatus(
            @PathVariable UUID ticketId,
            @Valid @RequestBody TicketDto.StatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        TicketDto.Response response = ticketService.updateTicketStatus(ticketId, request, user);
        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId + "/status", response);
        return response;
    }
}
