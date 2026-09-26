package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import com.example.demo.dto.CreateTicketRequest;
import com.example.demo.dto.TicketResponse;
import com.example.demo.entity.TicketEntity;
import com.example.demo.security.UserPrincipal;
import com.example.enums.TicketPriority;

public interface TicketService {

    List<TicketResponse> listTickets(Boolean active, TicketPriority priority, UserPrincipal user);

    TicketResponse createTicket(CreateTicketRequest request, UserPrincipal user);

    List<TicketEntity> findAll();

    List<TicketEntity> findByAccount(UUID accountId);

    TicketEntity save(TicketEntity ticket);
}
