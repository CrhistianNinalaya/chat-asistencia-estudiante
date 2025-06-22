package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ChatEntity;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Integer>{
	List<ChatEntity> findAllByPrioridad_CodPrioridad(Integer codPrioridad);
	List<ChatEntity> findAllByCuenta_CodUsuario(Integer codUsuario);
}
