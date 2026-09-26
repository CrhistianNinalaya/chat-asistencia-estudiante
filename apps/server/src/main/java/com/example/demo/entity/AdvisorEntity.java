package com.example.demo.entity;

import com.example.enums.AccountType;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADVISOR")
public class AdvisorEntity extends AccountEntity {

    @Override
    public AccountType getAccountType() {
        return AccountType.ADVISOR;
    }
}
