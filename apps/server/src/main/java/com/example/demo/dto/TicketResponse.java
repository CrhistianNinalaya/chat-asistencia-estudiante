package com.example.demo.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import com.example.demo.entity.TicketEntity;
import com.example.enums.TicketCategory;
import com.example.enums.TicketPriority;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        Instant startedAt,
        Instant closedAt,
        boolean active,
        TicketCategory category,
        TicketPriority priority,
        AccountResponse generatedBy,
        AccountResponse assignedTo
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static TicketResponse fromEntity(TicketEntity ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStartedAt(),
                ticket.getClosedAt(),
                ticket.isActive(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getGeneratedBy() != null ? AccountResponse.fromEntity(ticket.getGeneratedBy()) : null,
                ticket.getAssignedTo() != null ? AccountResponse.fromEntity(ticket.getAssignedTo()) : null
        );
    }
}
