package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TicketService;
import com.example.enums.AccountType;
import com.example.enums.TicketPriority;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/tickets"})
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public List<TicketEntity> listAll(
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "priority", required = false) TicketPriority priority,
            @AuthenticationPrincipal UserPrincipal user) {
        return ticketService.listTickets(active, priority, user);
    }

    @PostMapping
    public TicketEntity create(
            @RequestBody TicketEntity ticket,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null || user.getAccountType() != AccountType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can create tickets");
        }

        StudentEntity student = new StudentEntity();
        student.setId(user.getId());
        student.setFirstName(user.getFirstName());
        student.setLastName(user.getLastName());
        student.setEmail(user.getEmail());
        ticket.setGeneratedBy(student);

        return ticketService.save(ticket);
    }
}
