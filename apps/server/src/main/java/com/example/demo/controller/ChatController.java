package com.example.demo.controller;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.ChatEntity;
import com.example.demo.service.ChatService;
import com.example.enums.AccountType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    public List<ChatEntity> listAll(
            @RequestParam(value = "priority", required = false) UUID priorityId,
            HttpSession session) {

        AccountEntity u = (AccountEntity) session.getAttribute("user");
        boolean isAdvisor = u != null && u.getAccountType() == AccountType.ADVISOR;

        if (isAdvisor) {
            if (priorityId != null) {
                return chatService.findByPriority(priorityId);
            }
            return chatService.findAll();
        }

        UUID accountId = u != null ? u.getId() : null;
        List<ChatEntity> userChats = chatService.findByAccount(accountId);
        if (priorityId != null) {
            return userChats.stream()
                    .filter(c -> c.getPriority() != null && c.getPriority().getId().equals(priorityId))
                    .collect(Collectors.toList());
        }
        return userChats;
    }

    @PostMapping
    public ChatEntity create(@RequestBody ChatEntity chat) {
        return chatService.save(chat);
    }
}