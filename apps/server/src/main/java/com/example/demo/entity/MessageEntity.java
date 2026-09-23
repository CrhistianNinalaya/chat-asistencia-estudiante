package com.example.demo.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class MessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "sent_at", nullable = false, columnDefinition = "DATETIME")
	private LocalDateTime sentAt;

	@ManyToOne
	@JoinColumn(name = "chat_id", nullable = false)
	private ChatEntity chat;

	@ManyToOne
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public ChatEntity getChat() {
		return chat;
	}

	public void setChat(ChatEntity chat) {
		this.chat = chat;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public MessageEntity(Integer id, String content, LocalDateTime sentAt, ChatEntity chat, AccountEntity account) {
		this.id = id;
		this.content = content;
		this.sentAt = sentAt;
		this.chat = chat;
		this.account = account;
	}

	public MessageEntity() {
	}

	@Override
	public String toString() {
		return "MessageEntity [id=" + id + ", content=" + content + ", sentAt=" + sentAt + ", chat=" + chat
				+ ", account=" + account + "]";
	}
}
