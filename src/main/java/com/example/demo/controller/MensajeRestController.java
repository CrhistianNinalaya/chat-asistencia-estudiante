package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.CuentaEntity;
import com.example.demo.entity.MensajeEntity;
import com.example.demo.service.MensajeService;

@RestController
@RequestMapping("/api/chats/{codChat}/mensajes")
public class MensajeRestController {

	@Autowired
    private MensajeService mensajeService;

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
        return mensajeService.save(msg);
    }
}
