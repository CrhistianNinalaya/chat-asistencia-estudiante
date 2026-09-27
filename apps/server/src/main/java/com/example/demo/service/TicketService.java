package com.example.demo.service;

import java.util.List;
import com.example.demo.dto.TicketDto;
import com.example.demo.enums.TicketPriority;
import com.example.demo.security.UserPrincipal;

public interface TicketService {

    List<TicketDto.Response> listTickets(Boolean active, TicketPriority priority, UserPrincipal user);

    TicketDto.Response createTicket(TicketDto.CreateRequest request, UserPrincipal user);
}
