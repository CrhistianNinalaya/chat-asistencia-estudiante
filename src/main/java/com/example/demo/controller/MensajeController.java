package com.example.demo.controller;


import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.example.demo.service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.CuentaEntity;
import com.example.demo.entity.MensajeEntity;

@RestController
@RequestMapping("/api/chats/{codChat}/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;
    private final SimpMessagingTemplate messagingTemplate;

    public MensajeController(MensajeService mensajeService, SimpMessagingTemplate messagingTemplate) {
        this.mensajeService = mensajeService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<MensajeEntity> getByChat(@PathVariable Integer codChat) {
        return mensajeService.obtenerMensajesPorCodChat(codChat);
    }

    @PostMapping
    public MensajeEntity post(
            @PathVariable Integer codChat,
            @RequestBody MensajeEntity msg,
            HttpSession session
    ) {
        msg.setChat(new ChatEntity(codChat, null, null, null, false, null, null, null));
        msg.setFecMensaje(LocalDateTime.now());
        CuentaEntity u = (CuentaEntity) session.getAttribute("usuario");
        msg.setCuenta(u);
        MensajeEntity savedMsg = mensajeService.save(msg);

        messagingTemplate.convertAndSend("/topic/chat/" + codChat, savedMsg);

        return savedMsg;
    }
}