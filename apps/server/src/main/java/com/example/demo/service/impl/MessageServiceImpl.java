package com.example.demo.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.MessageEntity;
import com.example.demo.repository.MessageRepository;
import com.example.demo.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public List<MessageEntity> getMessagesByChatId(Integer chatId) {
        return messageRepository.findAllByChat_Id(chatId);
    }

    @Override
    public MessageEntity save(MessageEntity message) {
        return messageRepository.save(message);
    }
}
