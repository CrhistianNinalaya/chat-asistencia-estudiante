package com.example.demo.controller;
import com.example.demo.entity.DTO.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.CuentaEntity;
import com.example.demo.service.CuentaService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private CuentaService cuentaService;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
		boolean usuarioValido = cuentaService.validarUsuario(loginRequest);
		if(usuarioValido) {
			CuentaEntity cuentaLogeada = cuentaService.buscarUsuarioPorCorreo(loginRequest.getCorreo());
			return ResponseEntity.ok(cuentaLogeada);
		}
		return ResponseEntity.badRequest().body("Credenciales inválidas");
	}
}
