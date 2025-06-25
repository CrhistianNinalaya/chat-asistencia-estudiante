package com.example.demo.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CuentaEntity;
import com.example.demo.service.CuentaService;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
	@Autowired
    private CuentaService cuentaService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CuentaEntity cuenta, HttpSession session) {
        boolean ok = cuentaService.validarUsuario(cuenta, session);
        if (!ok) {
            return ResponseEntity
                     .status(HttpStatus.UNAUTHORIZED)
                     .body("Credenciales inválidas");
        }
        // guarda el usuario en sesión
        CuentaEntity u = cuentaService.buscarUsuarioPorCorreo(cuenta.getCorreo());
        session.setAttribute("usuario", u);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
