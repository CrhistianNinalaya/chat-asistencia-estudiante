package com.example.demo.service;

import java.util.Optional;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.AccountEntity;

public interface AccountService {

    AuthResponse authenticate(LoginRequest request);

    Optional<AccountEntity> findAccountByEmail(String email);
}
