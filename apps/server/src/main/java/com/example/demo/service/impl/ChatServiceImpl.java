package com.example.demo.service.impl;

import com.example.demo.entity.ChatEntity;
import com.example.demo.repository.ChatRepository;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Override
    public List<ChatEntity> findAll() {
        return chatRepository.findAll();
    }

    @Override
    public List<ChatEntity> findByPriority(UUID priorityId) {
        return chatRepository.findAllByPriority_Id(priorityId);
    }

    @Override
    public ChatEntity save(ChatEntity chat) {
        return chatRepository.save(chat);
    }

    @Override
    public List<ChatEntity> findByAccount(UUID accountId) {
        return chatRepository.findAllByAccount_Id(accountId);
    }
}
