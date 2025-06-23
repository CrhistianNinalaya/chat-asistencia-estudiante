package com.example.demo.service;

import com.example.demo.entity.ChatEntity;

import java.util.List;

public interface ChatService {
    List<ChatEntity> findByPrioridadCodPrioridad(Integer codPrioridad);
}
