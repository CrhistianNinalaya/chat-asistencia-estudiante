package com.example.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TicketAssignmentHistoryEntity;

public interface TicketAssignmentHistoryRepository extends JpaRepository<TicketAssignmentHistoryEntity, UUID> {

    @EntityGraph(attributePaths = {"ticket", "advisor"})
    @Query("""
        SELECT h FROM TicketAssignmentHistoryEntity h
        WHERE h.ticket.id = :ticketId
          AND h.unassignedAt IS NULL
        ORDER BY h.assignedAt DESC
    """)
    Optional<TicketAssignmentHistoryEntity> findActiveAssignment(@Param("ticketId") UUID ticketId);
}
