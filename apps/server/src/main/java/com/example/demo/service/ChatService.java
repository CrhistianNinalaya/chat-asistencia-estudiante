package com.example.demo.service;

import com.example.demo.entity.ChatEntity;
import java.util.List;

public interface ChatService {
    List<ChatEntity> findAll();
    List<ChatEntity> findByPriority(Integer priorityId);
    List<ChatEntity> findByAccount(Integer accountId);
    ChatEntity save(ChatEntity chat);
}
