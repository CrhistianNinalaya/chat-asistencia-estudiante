package com.example.demo.service;

import java.util.List;
import java.util.UUID;

import com.example.demo.entity.MessageEntity;

public interface MessageService {
    List<MessageEntity> getMessagesByChatId(UUID chatId);

    MessageEntity save(MessageEntity message);
}
