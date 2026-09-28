package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TicketActivityEntity;

public interface TicketActivityRepository extends JpaRepository<TicketActivityEntity, UUID> {

    @EntityGraph(attributePaths = {"ticket", "actor"})
    List<TicketActivityEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
