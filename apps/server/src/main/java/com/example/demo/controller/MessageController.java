package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.MessageDto;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<MessageDto.Response> getByChat(@PathVariable UUID ticketId) {
        return messageService.getMessagesByChatId(ticketId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto.Response sendMessage(
            @PathVariable UUID ticketId,
            @Valid @RequestBody MessageDto.SendRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be authenticated");
        }

        MessageDto.Response response = messageService.sendMessage(request, ticketId, user);
        messagingTemplate.convertAndSend("/topic/chat/" + ticketId, response);
        return response;
    }
}
