package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.service.MessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<MessageEntity> getByChat(@PathVariable Integer chatId) {
        return messageService.getMessagesByChatId(chatId);
    }

    @PostMapping
    public MessageEntity post(
            @PathVariable Integer chatId,
            @RequestBody MessageEntity msg,
            HttpSession session
    ) {
        msg.setChat(new ChatEntity(chatId, null, null, null, false, null, null, null));
        msg.setSentAt(LocalDateTime.now());
        AccountEntity u = (AccountEntity) session.getAttribute("user");
        msg.setAccount(u);
        MessageEntity savedMsg = messageService.save(msg);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMsg);

        return savedMsg;
    }
}
