package com.example.demo.controller;


import java.util.List;

import com.example.demo.service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.MensajeEntity;

@RestController
@RequestMapping("/api/mensaje")
public class MensajeController {
	@Autowired
	private MensajeService mensajeService;

	@GetMapping("/chat")
	public ResponseEntity<?> showChat(
			@RequestParam(value = "codChat", required = false) Integer codChat) {
		try {
			if (codChat == null) {
				List<MensajeEntity> mensajes = mensajeService.obtenerMensajes();
				return mensajes.isEmpty() ?
						ResponseEntity.notFound().build() :
						ResponseEntity.ok(mensajes);
			}
			if (codChat == 0) {
				return ResponseEntity.badRequest().body("El código del chat no puede ser cero.");
			}
			List<MensajeEntity> mensajes = mensajeService.obtenerMensajesPorCodChat(codChat);
			return mensajes.isEmpty() ?
					ResponseEntity.notFound().build() :
					ResponseEntity.ok(mensajes);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al obtener los mensajes: " + e.getMessage());
		}
	}

	@PostMapping("/enviar")
	public ResponseEntity<?> registrarMensaje(@RequestBody MensajeEntity mensaje) {
		try {
			mensajeService.guardarMensaje(mensaje);
			return ResponseEntity.ok().body(mensaje);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al registrar el mensaje: " + e.getMessage());
		}
	}
}