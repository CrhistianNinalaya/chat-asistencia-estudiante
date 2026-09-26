package com.example.demo.service.impl;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.AccountEntity;
import com.example.demo.repository.AccountRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        AccountEntity account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserPrincipal principal = UserPrincipal.fromEntity(account);
        String token = jwtService.generateToken(principal);
        AccountResponse accountResponse = AccountResponse.fromEntity(account);

        return new AuthResponse(token, accountResponse);
    }

    @Override
    public Optional<AccountEntity> findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
}
