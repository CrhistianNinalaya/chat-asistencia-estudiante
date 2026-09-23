package com.example.demo.service;

import com.example.demo.entity.ChatEntity;
import java.util.List;

public interface ChatService {
	List<ChatEntity> findAll();
    List<ChatEntity> findByPrioridad(Integer codPrioridad);
    List<ChatEntity> findByUsuario(Integer codUsuario);
    ChatEntity save(ChatEntity chat);
}
