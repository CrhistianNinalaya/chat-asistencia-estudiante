package com.example.demo.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import com.example.demo.entity.TicketEntity;
import com.example.demo.enums.ResolutionCategory;
import com.example.demo.enums.TicketCategory;
import com.example.demo.enums.TicketPriority;
import com.example.demo.enums.TicketStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TicketDto {

    private TicketDto() {
    }

    @Schema(name = "TicketCreateRequest")
    public record CreateRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must not exceed 255 characters")
            String title,
            @NotBlank(message = "Description is required")
            String description,
            @NotNull(message = "Category is required")
            TicketCategory category
            ) {

    }

    @Schema(name = "TicketStatusUpdateRequest")
    public record StatusUpdateRequest(
            @NotNull(message = "Status is required")
            TicketStatus status,
            String resolutionSummary,
            ResolutionCategory resolutionCategory
            ) {

    }

    @Schema(name = "TicketResponse")
    public record Response(
            UUID id,
            String title,
            String description,
            Instant startedAt,
            Instant closedAt,
            TicketStatus status,
            String resolutionSummary,
            ResolutionCategory resolutionCategory,
            TicketCategory category,
            TicketPriority priority,
            StudentDto.Response generatedBy,
            AdvisorDto.Response assignedTo
            ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static Response fromEntity(TicketEntity ticket) {
            return new Response(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getStartedAt(),
                    ticket.getClosedAt(),
                    ticket.getStatus(),
                    ticket.getResolutionSummary(),
                    ticket.getResolutionCategory(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getGeneratedBy() != null ? StudentDto.Response.fromEntity(ticket.getGeneratedBy()) : null,
                    ticket.getAssignedTo() != null ? AdvisorDto.Response.fromEntity(ticket.getAssignedTo()) : null
            );
        }
    }
}
