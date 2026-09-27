package com.example.demo.service;

import com.example.demo.dto.AuthDto;

public interface AccountService {

    AuthDto.Response authenticate(AuthDto.LoginRequest request);
}
