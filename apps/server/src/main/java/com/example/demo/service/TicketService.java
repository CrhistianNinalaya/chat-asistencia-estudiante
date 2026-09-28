package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import com.example.demo.dto.TicketDto;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;
import com.example.demo.security.UserPrincipal;

public interface TicketService {

    List<TicketDto.Response> listTickets(TicketStatus status, TicketPriority priority, UserPrincipal user);

    TicketDto.Response createTicket(TicketDto.CreateRequest request, UserPrincipal user);

    TicketDto.Response updateTicketStatus(UUID ticketId, TicketDto.StatusUpdateRequest request, UserPrincipal user);
}
