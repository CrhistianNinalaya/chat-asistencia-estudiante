package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.MensajeEntity;

public interface MensajeService {
    List<MensajeEntity> obtenerMensajes();
    List<MensajeEntity> obtenerMensajesPorCodChat(Integer codChat);
    MensajeEntity guardarMensaje(MensajeEntity mensaje);
}
