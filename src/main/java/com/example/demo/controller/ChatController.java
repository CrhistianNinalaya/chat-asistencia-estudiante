package com.example.demo.controller;

import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.CuentaEntity;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

	@Autowired
    private ChatService chatService;

 @GetMapping
    public List<ChatEntity> listAll(
            @RequestParam(value="priority", required=false) Integer codPrioridad,
            HttpSession session) {

        CuentaEntity u = (CuentaEntity) session.getAttribute("usuario");
        boolean isAdvisor = u.getTipo().getCodTipo() == 1;

        if (isAdvisor) {
            if (codPrioridad != null) {
                return chatService.findByPrioridad(codPrioridad);
            }
            return chatService.findAll();
        }

        List<ChatEntity> userChats = chatService.findByUsuario(u.getCodUsuario());
        if (codPrioridad != null) {
            return userChats.stream()
                    .filter(c -> c.getPrioridad().getCodPrioridad().equals(codPrioridad))
                    .collect(Collectors.toList());
        }
        return userChats;
    }

    @PostMapping
    public ChatEntity create(@RequestBody ChatEntity chat) {
        return chatService.save(chat);
    }
}