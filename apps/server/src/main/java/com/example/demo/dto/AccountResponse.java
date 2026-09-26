package com.example.demo.dto;

import java.io.Serializable;
import java.util.UUID;

import com.example.demo.entity.AccountEntity;
import com.example.enums.AccountType;

public record AccountResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        AccountType accountType) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static AccountResponse fromEntity(AccountEntity accountEntity) {
        return new AccountResponse(
                accountEntity.getId(),
                accountEntity.getFirstName(),
                accountEntity.getLastName(),
                accountEntity.getEmail(),
                accountEntity.getAccountType());
    }
}
