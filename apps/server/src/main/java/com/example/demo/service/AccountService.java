package com.example.demo.service;

import java.util.Optional;

import com.example.demo.dto.AuthDto;
import com.example.demo.entity.AccountEntity;

public interface AccountService {

    AuthDto.Response authenticate(AuthDto.LoginRequest request);

    Optional<AccountEntity> findAccountByEmail(String email);
}
