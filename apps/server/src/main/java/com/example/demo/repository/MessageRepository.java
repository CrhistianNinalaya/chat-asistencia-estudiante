package com.example.demo.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    @EntityGraph(attributePaths = {"account"})
    @Query("SELECT m FROM MessageEntity m WHERE m.ticket.id = :ticketId ORDER BY m.sentAt DESC")
    List<MessageEntity> findRecentMessages(
            @Param("ticketId") UUID ticketId,
            Pageable pageable);

    @EntityGraph(attributePaths = {"account"})
    @Query("SELECT m FROM MessageEntity m WHERE m.ticket.id = :ticketId AND m.sentAt < :before ORDER BY m.sentAt DESC")
    List<MessageEntity> findMessagesBefore(
            @Param("ticketId") UUID ticketId,
            @Param("before") Instant before,
            Pageable pageable);
}
