package com.example.demo.service;

import jakarta.servlet.http.HttpSession;
import com.example.demo.entity.AccountEntity;

public interface AccountService {
    boolean validateAccount(AccountEntity account, HttpSession session);
    AccountEntity findAccountByEmail(String email);
}
