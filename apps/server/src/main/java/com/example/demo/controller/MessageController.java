package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.MessageService;
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

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
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

        msg.setChat(new ChatEntity(chatId, null, null, null, false, null, null, null));
        msg.setSentAt(LocalDateTime.now(ZoneOffset.UTC));

        AccountEntity author = new AccountEntity();
        author.setId(user.getId());
        author.setFirstName(user.getFirstName());
        author.setLastName(user.getLastName());
        author.setEmail(user.getEmail());
        author.setAccountType(user.getAccountType());
        msg.setAccount(author);

        MessageEntity savedMsg = messageService.save(msg);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMsg);

        return savedMsg;
    }
}
