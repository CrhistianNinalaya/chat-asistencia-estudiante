package com.example.demo.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    // =========================================================================
    // Advisor Queries
    // =========================================================================
    @Override
    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    Optional<TicketEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status NOT IN (TicketStatus.RESOLVED, TicketStatus.CLOSED)
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisor(@Param("advisorId") UUID advisorId);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status NOT IN (TicketStatus.RESOLVED, TicketStatus.CLOSED)
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
          AND t.startedAt > :after
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisorStartedAfter(
            @Param("advisorId") UUID advisorId,
            @Param("after") Instant after);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status NOT IN (TicketStatus.RESOLVED, TicketStatus.CLOSED)
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
          AND t.startedAt <= :beforeAndEqual
          AND t.startedAt > :after
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisorStartedBetween(
            @Param("advisorId") UUID advisorId,
            @Param("beforeAndEqual") Instant beforeAndEqual,
            @Param("after") Instant after);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status NOT IN (TicketStatus.RESOLVED, TicketStatus.CLOSED)
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
          AND t.startedAt <= :beforeAndEqual
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisorStartedBeforeOrEqual(
            @Param("advisorId") UUID advisorId,
            @Param("beforeAndEqual") Instant beforeAndEqual);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status IN (TicketStatus.RESOLVED, TicketStatus.CLOSED)
          AND t.assignedTo.id = :advisorId
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findClosedForAdvisor(@Param("advisorId") UUID advisorId);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.status = :status
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findAssignedOrUnassignedByStatus(
            @Param("advisorId") UUID advisorId,
            @Param("status") TicketStatus status);

    // =========================================================================
    // Student Queries
    // =========================================================================
    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.generatedBy.id = :studentId
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findAllByStudent(@Param("studentId") UUID studentId);

    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.generatedBy.id = :studentId
          AND t.status = :status
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findAllByStudentAndStatus(
            @Param("studentId") UUID studentId,
            @Param("status") TicketStatus status);

    // =========================================================================
    // Admin Queries
    // =========================================================================
    @EntityGraph(attributePaths = {"generatedBy", "assignedTo"})
    @Query("""
        SELECT t FROM TicketEntity t
        WHERE (:status IS NULL OR t.status = :status)
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findAllForAdmin(@Param("status") TicketStatus status);
}
