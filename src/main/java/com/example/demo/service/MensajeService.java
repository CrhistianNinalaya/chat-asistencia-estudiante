package com.example.demo.service;

import java.util.List;
import com.example.demo.entity.MensajeEntity;

public interface MensajeService {
	List<MensajeEntity> obtenerMensajesPorCodChat(Integer codChat);
    MensajeEntity save(MensajeEntity mensaje);
}
