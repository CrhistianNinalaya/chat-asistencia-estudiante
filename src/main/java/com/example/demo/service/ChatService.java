package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.ChatEntity;

public interface ChatService {
	List<ChatEntity> findAll();
    List<ChatEntity> findByPrioridad(Integer codPrioridad);
    List<ChatEntity> findByUsuario(Integer codUsuario);
    ChatEntity save(ChatEntity chat);
}
