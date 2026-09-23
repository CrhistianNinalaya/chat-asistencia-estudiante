package com.example.demo.service;

import java.util.List;
import com.example.demo.entity.MessageEntity;

public interface MessageService {
    List<MessageEntity> getMessagesByChatId(Integer chatId);
    MessageEntity save(MessageEntity message);
}
