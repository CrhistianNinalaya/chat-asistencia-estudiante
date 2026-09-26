package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CreateTicketRequest;
import com.example.demo.dto.TicketResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;
import com.example.enums.TicketPriority;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/tickets"})
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketResponse> listAll(
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "priority", required = false) TicketPriority priority,
            @AuthenticationPrincipal UserPrincipal user) {
        return ticketService.listTickets(active, priority, user);
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        TicketResponse response = ticketService.createTicket(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
