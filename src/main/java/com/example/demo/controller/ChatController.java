package com.example.demo.controller;

import com.example.demo.entity.ChatEntity;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public String sendMessage(String message) {
        return message;
    }

	@GetMapping("/filtrar")
	public ResponseEntity<?> filtrarChat(@RequestParam("codPrioridad") Integer codPrioridad) {
        try {
            List<ChatEntity> chats = chatService.findByPrioridadCodPrioridad(codPrioridad);
            return chats.isEmpty() ?
                    ResponseEntity.notFound().build() :
                    ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al filtrar los chats: " + e.getMessage());
        }
	}
}