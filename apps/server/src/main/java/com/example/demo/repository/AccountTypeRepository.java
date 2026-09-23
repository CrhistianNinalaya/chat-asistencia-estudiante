package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AccountTypeEntity;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountTypeEntity, Integer> {
}
