package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "first_name", nullable = false, columnDefinition = "VARCHAR(50)")
	private String firstName;

	@Column(name = "last_name", nullable = false, columnDefinition = "VARCHAR(50)")
	private String lastName;

	@Column(name = "password", nullable = false, columnDefinition = "CHAR(6)")
	private String password;

	@Column(name = "email", nullable = false, columnDefinition = "VARCHAR(50)", unique = true)
	private String email;

	@ManyToOne
	@JoinColumn(name = "account_type_id", nullable = false)
	private AccountTypeEntity accountType;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public AccountTypeEntity getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountTypeEntity accountType) {
		this.accountType = accountType;
	}

	public AccountEntity() {
	}

	public AccountEntity(Integer id, String firstName, String lastName, String password, String email, AccountTypeEntity accountType) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
		this.accountType = accountType;
	}

	@Override
	public String toString() {
		return "AccountEntity [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", password="
				+ password + ", email=" + email + ", accountType=" + accountType + "]";
	}
}
