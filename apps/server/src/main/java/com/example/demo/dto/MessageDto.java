package com.example.demo.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.demo.entity.AccountEntity;
import com.example.demo.entity.MessageEntity;
import com.example.demo.enums.AccountType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MessageDto {

    private MessageDto() {
    }

    @Schema(name = "MessageSendRequest")
    public record SendRequest(
            @NotBlank(message = "Message content must not be blank")
            @Size(max = 2000, message = "Message content must not exceed 2000 characters")
            String content
            ) {

    }

    @Schema(name = "MessageResponse")
    public record Response(
            UUID id,
            String content,
            Instant sentAt,
            UUID ticketId,
            UUID senderId,
            String senderName,
            AccountType senderRole
            ) implements Serializable {

        private static final long serialVersionUID = 1L;

        public static Response fromEntity(MessageEntity entity) {
            AccountEntity account = entity.getAccount();
            String fullName = account.getFirstName() + " " + account.getLastName();
            return new Response(
                    entity.getId(),
                    entity.getContent(),
                    entity.getSentAt(),
                    entity.getTicket().getId(),
                    account.getId(),
                    fullName,
                    account.getAccountType()
            );
        }
    }

    @Schema(name = "MessagePagedResponse")
    public record PagedResponse(
            List<Response> messages,
            boolean hasMore,
            Instant nextCursor
            ) implements Serializable {

        private static final long serialVersionUID = 1L;
    }
}
