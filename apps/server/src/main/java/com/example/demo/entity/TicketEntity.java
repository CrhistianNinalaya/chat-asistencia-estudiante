package com.example.demo.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.example.demo.enums.ResolutionCategory;
import com.example.demo.enums.TicketCategory;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;

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

    @Column(name = "started_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant startedAt;

    @Column(name = "closed_at", columnDefinition = "TIMESTAMP(6)")
    private Instant closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private StudentEntity generatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private AdvisorEntity assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_category", length = 50)
    private ResolutionCategory resolutionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TicketCategory category;

    public static final long LOW_PRIORITY_MAX_DAYS = 2L;
    public static final long MEDIUM_PRIORITY_MAX_DAYS = 4L;

    @Transient
    public boolean isActive() {
        return this.status != TicketStatus.RESOLVED && this.status != TicketStatus.CLOSED;
    }

    @Transient
    public TicketPriority getPriority() {
        Instant reference = this.closedAt != null ? this.closedAt : Instant.now();
        Instant start = this.startedAt != null ? this.startedAt : reference;
        long days = Duration.between(start, reference).toDays();
        if (days < LOW_PRIORITY_MAX_DAYS) {
            return TicketPriority.LOW;
        } else if (days < MEDIUM_PRIORITY_MAX_DAYS) {
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
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
    }
}
