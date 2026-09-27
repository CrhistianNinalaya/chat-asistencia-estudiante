package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import com.example.demo.dto.MessageDto;
import com.example.demo.security.UserPrincipal;

public interface MessageService {

    List<MessageDto.Response> getMessagesByChatId(UUID ticketId);

    MessageDto.Response sendMessage(MessageDto.SendRequest request, UUID ticketId, UserPrincipal user);
}
