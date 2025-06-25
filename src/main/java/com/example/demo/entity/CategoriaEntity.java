	package com.example.demo.entity;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tb_categoria")
public class CategoriaEntity{
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cod_cat", nullable = false, unique = true)
	private Integer codCateogira;
	
	@Column(name="nombre", nullable = false, columnDefinition = "VARCHAR(20)", unique = true)
	private String nombre;

	public Integer getCodCateogira() {
		return codCateogira;
	}

	public void setCodCateogira(Integer codCateogira) {
		this.codCateogira = codCateogira;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public CategoriaEntity(Integer codCateogira, String nombre) {
		super();
		this.codCateogira = codCateogira;
		this.nombre = nombre;
	}

	public CategoriaEntity() {}

	@Override
	public String toString() {
		return "CategoriaEntity [codCateogira=" + codCateogira + ", nombre=" + nombre + "]";
	}
}
