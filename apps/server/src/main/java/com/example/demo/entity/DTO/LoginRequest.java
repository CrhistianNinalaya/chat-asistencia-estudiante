package com.example.demo.entity.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String correo;
    private String password;
}
