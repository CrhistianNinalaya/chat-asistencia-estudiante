package com.example.demo.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "categories")
public class CategoryEntity {

	@Id
	@GeneratedValue
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "name", nullable = false, length = 20, unique = true)
	private String name;
}
