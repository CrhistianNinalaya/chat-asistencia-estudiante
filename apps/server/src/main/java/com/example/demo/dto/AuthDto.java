package com.example.demo.dto;

import java.io.Serializable;
import java.util.UUID;

import com.example.demo.entity.AccountEntity;
import com.example.enums.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDto {

    private AuthDto() {}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record AccountResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            AccountType accountType
    ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static AccountResponse fromEntity(AccountEntity entity) {
            return new AccountResponse(
                    entity.getId(),
                    entity.getFirstName(),
                    entity.getLastName(),
                    entity.getEmail(),
                    entity.getAccountType()
            );
        }
    }

    public record Response(
            String token,
            AccountResponse user
    ) implements Serializable {

        private static final long serialVersionUID = 1L;
    }
}
