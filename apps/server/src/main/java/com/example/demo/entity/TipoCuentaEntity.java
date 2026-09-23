package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tb_tipo_cuenta")
public class TipoCuentaEntity{
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cod_tipo", nullable = false, unique = true)
	private Integer codTipo;
	
	@Column(name="nom_tipo", nullable = false, columnDefinition = "VARCHAR(20)", unique = true)
	private String nomTipo;

	public Integer getCodTipo() {
		return codTipo;
	}

	public void setCodTipo(Integer codTipo) {
		this.codTipo = codTipo;
	}

	public String getNomTipo() {
		return nomTipo;
	}

	public void setNomTipo(String nomTipo) {
		this.nomTipo = nomTipo;
	}

	public TipoCuentaEntity(Integer codTipo, String nomTipo) {
		this.codTipo = codTipo;
		this.nomTipo = nomTipo;
	}

	public TipoCuentaEntity() {
	}

	@Override
	public String toString() {
		return "TipoCuentaEntity [codTipo=" + codTipo + ", nomTipo=" + nomTipo + "]";
	}

	
}
