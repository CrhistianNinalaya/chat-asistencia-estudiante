package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ChatEntity;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
    List<ChatEntity> findAllByPriority_Id(UUID priorityId);

    List<ChatEntity> findAllByAccount_Id(UUID accountId);
}
