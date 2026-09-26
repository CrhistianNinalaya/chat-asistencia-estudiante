package com.example.demo.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TicketEntity;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findAllByGeneratedBy_Id(UUID studentId);

    List<TicketEntity> findAllByAssignedTo_Id(UUID advisorId);

    // =========================================================================
    // Advisor Queries
    // =========================================================================

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.active = true 
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisor(@Param("advisorId") UUID advisorId);

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.active = true 
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
          AND t.startedAt > :after
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisorStartedAfter(
            @Param("advisorId") UUID advisorId,
            @Param("after") Instant after);

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.active = true 
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

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.active = true 
          AND (t.assignedTo.id = :advisorId OR t.assignedTo IS NULL)
          AND t.startedAt <= :beforeAndEqual
        ORDER BY 
            CASE WHEN t.assignedTo.id = :advisorId THEN 1 ELSE 2 END ASC,
            t.startedAt ASC
    """)
    List<TicketEntity> findActiveForAdvisorStartedBeforeOrEqual(
            @Param("advisorId") UUID advisorId,
            @Param("beforeAndEqual") Instant beforeAndEqual);

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.active = false 
          AND t.assignedTo.id = :advisorId
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findClosedForAdvisor(@Param("advisorId") UUID advisorId);

    // =========================================================================
    // Student Queries
    // =========================================================================

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.generatedBy.id = :studentId
        ORDER BY t.active DESC, t.startedAt DESC
    """)
    List<TicketEntity> findAllByStudent(@Param("studentId") UUID studentId);

    @Query("""
        SELECT t FROM TicketEntity t
        WHERE t.generatedBy.id = :studentId
          AND t.active = :active
        ORDER BY t.startedAt DESC
    """)
    List<TicketEntity> findAllByStudentAndActive(
            @Param("studentId") UUID studentId,
            @Param("active") boolean active);
}
