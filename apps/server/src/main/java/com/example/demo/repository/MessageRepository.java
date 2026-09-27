package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    List<MessageEntity> findAllByChat_IdOrderBySentAtAsc(UUID chatId);
}
