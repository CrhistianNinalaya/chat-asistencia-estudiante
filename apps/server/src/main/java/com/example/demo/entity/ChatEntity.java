package com.example.demo.entity;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chats")
public class ChatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "start_date", nullable = false, columnDefinition = "DATE")
	private LocalDate startDate;

	@Column(name = "closed_date", nullable = false, columnDefinition = "DATE")
	private LocalDate closedDate;

	@Column(name = "active", nullable = false)
	private boolean active;

	@ManyToOne
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@ManyToOne
	@JoinColumn(name = "priority_id", nullable = false)
	private PriorityEntity priority;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(LocalDate closedDate) {
		this.closedDate = closedDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public CategoryEntity getCategory() {
		return category;
	}

	public void setCategory(CategoryEntity category) {
		this.category = category;
	}

	public PriorityEntity getPriority() {
		return priority;
	}

	public void setPriority(PriorityEntity priority) {
		this.priority = priority;
	}

	public ChatEntity(Integer id, String subject, LocalDate startDate, LocalDate closedDate, boolean active,
			AccountEntity account, CategoryEntity category, PriorityEntity priority) {
		super();
		this.id = id;
		this.subject = subject;
		this.startDate = startDate;
		this.closedDate = closedDate;
		this.active = active;
		this.account = account;
		this.category = category;
		this.priority = priority;
	}

	public ChatEntity() {
	}

	@Override
	public String toString() {
		return "ChatEntity [id=" + id + ", subject=" + subject + ", startDate=" + startDate + ", closedDate="
				+ closedDate + ", active=" + active + ", account=" + account + ", category=" + category
				+ ", priority=" + priority + "]";
	}
}
