package com.example.demo.service;

import java.time.Instant;
import java.util.UUID;

import com.example.demo.dto.MessageDto;
import com.example.demo.security.UserPrincipal;

public interface MessageService {

    MessageDto.PagedResponse getMessagesByTicketId(UUID ticketId, Instant before, int limit, UserPrincipal user);

    MessageDto.Response sendMessage(MessageDto.SendRequest request, UUID ticketId, UserPrincipal user);
}
