package com.example.demo.service;

import com.example.demo.entity.ChatEntity;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    List<ChatEntity> findAll();

    List<ChatEntity> findByPriority(UUID priorityId);

    List<ChatEntity> findByAccount(UUID accountId);

    ChatEntity save(ChatEntity chat);
}
