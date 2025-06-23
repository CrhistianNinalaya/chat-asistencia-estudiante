package com.example.demo.service;

import javax.servlet.http.HttpSession;

import com.example.demo.entity.CuentaEntity;
import com.example.demo.entity.DTO.LoginRequest;

public interface CuentaService {
	boolean validarUsuario(LoginRequest loginRequest);
	
	CuentaEntity buscarUsuarioPorCorreo(String correo);
}
