package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CategoriaEntity;

public interface CategoriaRepository  extends JpaRepository<CategoriaEntity, Integer>{

}
