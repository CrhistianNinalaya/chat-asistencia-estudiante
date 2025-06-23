package com.example.demo.service.impl;

import com.example.demo.entity.ChatEntity;
import com.example.demo.repository.ChatRepository;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatRepository chatRepository;

    @Override
    public List<ChatEntity> findByPrioridadCodPrioridad(Integer codPrioridad) {
        return chatRepository.findByPrioridadCodPrioridad(codPrioridad);
    }
}
