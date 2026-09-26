package com.example.demo.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import com.example.demo.entity.TicketEntity;
import com.example.enums.TicketCategory;
import com.example.enums.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TicketDto {

    private TicketDto() {}

    public record CreateRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must not exceed 255 characters")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            @NotNull(message = "Category is required")
            TicketCategory category
    ) {}

    public record Response(
            UUID id,
            String title,
            String description,
            Instant startedAt,
            Instant closedAt,
            boolean active,
            TicketCategory category,
            TicketPriority priority,
            AuthDto.AccountResponse generatedBy,
            AuthDto.AccountResponse assignedTo
    ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static Response fromEntity(TicketEntity ticket) {
            return new Response(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getStartedAt(),
                    ticket.getClosedAt(),
                    ticket.isActive(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getGeneratedBy() != null ? AuthDto.AccountResponse.fromEntity(ticket.getGeneratedBy()) : null,
                    ticket.getAssignedTo() != null ? AuthDto.AccountResponse.fromEntity(ticket.getAssignedTo()) : null
            );
        }
    }
}
