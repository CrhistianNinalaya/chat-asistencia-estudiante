package com.example.demo.entity;

import com.example.demo.enums.AccountType;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class AdminEntity extends AccountEntity {

    @Override
    public AccountType getAccountType() {
        return AccountType.ADMIN;
    }
}
