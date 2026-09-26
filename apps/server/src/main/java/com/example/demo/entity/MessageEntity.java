package com.example.demo.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sent_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private TicketEntity chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

}
