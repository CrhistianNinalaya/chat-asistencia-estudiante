package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.PrioridadEntity;

@Repository
public interface PrioridadRepository extends JpaRepository<PrioridadEntity, Integer>{

}
