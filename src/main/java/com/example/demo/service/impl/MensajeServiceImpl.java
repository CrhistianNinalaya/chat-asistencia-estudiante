package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.MensajeEntity;
import com.example.demo.repository.MensajeRepository;
import com.example.demo.service.MensajeService;

@Service
public class MensajeServiceImpl implements MensajeService {

	@Autowired
	private MensajeRepository mensajeRepository;

	@Override
	public List<MensajeEntity> obtenerMensajesPorCodChat(Integer codChat) {
		return mensajeRepository.findAllByChat_CodChat(codChat);
	}

	@Override
	public MensajeEntity save(MensajeEntity mensaje) {
		return mensajeRepository.save(mensaje);
	}
}
