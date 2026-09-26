package com.example.demo.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.example.enums.TicketCategory;
import com.example.enums.TicketPriority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"generatedBy", "assignedTo"})
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private StudentEntity generatedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private AdvisorEntity assignedTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TicketCategory category;

    @Transient
    public TicketPriority getPriority() {
        Instant reference = this.closedAt != null ? this.closedAt : Instant.now();
        Instant start = this.startedAt != null ? this.startedAt : reference;
        long days = Duration.between(start, reference).toDays();
        if (days < 2) {
            return TicketPriority.LOW;
        } else if (days < 4) {
            return TicketPriority.MEDIUM;
        } else {
            return TicketPriority.HIGH;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }
}
