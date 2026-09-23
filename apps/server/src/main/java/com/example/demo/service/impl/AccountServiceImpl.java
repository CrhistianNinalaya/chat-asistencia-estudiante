package com.example.demo.service.impl;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AccountEntity;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean validateAccount(AccountEntity account, HttpSession session) {
        AccountEntity accountFoundByEmail = accountRepository.findByEmail(account.getEmail());

        if (accountFoundByEmail == null) {
            return false;
        }

        if (account.getPassword().equals(accountFoundByEmail.getPassword())) {
            session.setAttribute("user", accountFoundByEmail.getEmail());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AccountEntity findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
}
