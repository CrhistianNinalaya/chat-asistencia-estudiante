package com.example.demo.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_assignment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentHistoryEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketEntity ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id", nullable = false)
    private AdvisorEntity advisor;

    @Column(name = "assigned_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant assignedAt;

    @Column(name = "unassigned_at", columnDefinition = "TIMESTAMP(6)")
    private Instant unassignedAt;

    @Column(name = "reason", length = 100)
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (this.assignedAt == null) {
            this.assignedAt = Instant.now();
        }
    }
}
