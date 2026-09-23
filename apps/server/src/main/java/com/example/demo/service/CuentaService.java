package com.example.demo.service;

import javax.servlet.http.HttpSession;
import com.example.demo.entity.CuentaEntity;

public interface CuentaService {
	boolean validarUsuario(CuentaEntity cuentaEntity, HttpSession session);
	CuentaEntity buscarUsuarioPorCorreo(String correo);
}
