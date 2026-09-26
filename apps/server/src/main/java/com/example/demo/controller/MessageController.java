package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.AdvisorEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.entity.StudentEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.MessageService;
import com.example.enums.AccountType;

@RestController
@RequestMapping({"/api/tickets/{chatId}/messages"})
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<MessageEntity> getByChat(@PathVariable UUID chatId) {
        return messageService.getMessagesByChatId(chatId);
    }

    @PostMapping
    public MessageEntity post(
            @PathVariable UUID chatId,
            @RequestBody MessageEntity msg,
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be authenticated");
        }

        TicketEntity ticket = new TicketEntity();
        ticket.setId(chatId);
        msg.setChat(ticket);
        msg.setSentAt(LocalDateTime.now(ZoneOffset.UTC));

        AccountEntity author = user.getAccountType() == AccountType.STUDENT
                ? new StudentEntity()
                : new AdvisorEntity();
        author.setId(user.getId());
        author.setFirstName(user.getFirstName());
        author.setLastName(user.getLastName());
        author.setEmail(user.getEmail());
        msg.setAccount(author);

        MessageEntity savedMsg = messageService.save(msg);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMsg);

        return savedMsg;
    }
}
