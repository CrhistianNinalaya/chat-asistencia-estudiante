package com.example.demo.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_prioridad")
public class PrioridadEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PrioridadEntity(Integer codPrioridad, String nombre) {
		super();
		this.codPrioridad = codPrioridad;
		this.nombre = nombre;
	}

	public PrioridadEntity() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cod_prioridad", nullable = false, unique = true)
	private Integer codPrioridad;

	@Column(name = "nombre", nullable = false, columnDefinition = "VARCHAR(25)", unique = true)
	private String nombre;

	public Integer getCodPrioridad() {
		return codPrioridad;
	}

	public void setCodPrioridad(Integer codPrioridad) {
		this.codPrioridad = codPrioridad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return "PrioridadEntity [codPrioridad=" + codPrioridad + ", nombre=" + nombre + "]";
	}

	
}
