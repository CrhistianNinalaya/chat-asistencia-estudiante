package com.example.demo.service.impl;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CuentaEntity;
import com.example.demo.repository.CuentaRepository;
import com.example.demo.service.CuentaService;

@Service
public class CuentaServiceImpl implements CuentaService {

	@Autowired
	private CuentaRepository cuentaRepository;

	@Override
	public boolean validarUsuario(CuentaEntity cuentaEntity, HttpSession session) {
		CuentaEntity cuentaEncontradaPorCorreo = cuentaRepository.findByCorreo(cuentaEntity.getCorreo());

		if (cuentaEncontradaPorCorreo == null) {
			return false;
		}

		if (cuentaEntity.getPassword().equals(cuentaEncontradaPorCorreo.getPassword())) {
			session.setAttribute("usuario", cuentaEncontradaPorCorreo.getCorreo());			
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public CuentaEntity buscarUsuarioPorCorreo(String correo) {
		return cuentaRepository.findByCorreo(correo);
	}

}
