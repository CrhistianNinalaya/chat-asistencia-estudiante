package com.example.demo.entity;


import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tb_cuenta")
public class CuentaEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cod_usuario", nullable = false)
	private Integer codUsuario;
	
	@Column(name="nom_usuario",nullable = false, columnDefinition = "VARCHAR(50)" )
	private String nombre;
	
	@Column(name="ape_usuario", nullable = false, columnDefinition = "VARCHAR(50)")
	private String apeUsuario;
	
	@Column(name="password", nullable = false, columnDefinition = "CHAR(6)")
	private String password;
	
	@Column(name="correo", nullable = false, columnDefinition = "VARCHAR(50)",unique = true)
	private String correo;
	
	@ManyToOne
	@JoinColumn(name="cod_tipo", nullable= false)
	private TipoCuentaEntity tipo;

	public Integer getCodUsuario() {
		return codUsuario;
	}

	public void setCodUsuario(Integer codUsuario) {
		this.codUsuario = codUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApeUsuario() {
		return apeUsuario;
	}

	public void setApeUsuario(String apeUsuario) {
		this.apeUsuario = apeUsuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public TipoCuentaEntity getTipo() {
		return tipo;
	}

	public void setTipo(TipoCuentaEntity tipo) {
		this.tipo = tipo;
	}

	public CuentaEntity() {
	}

	@Override
	public String toString() {
		return "CuentaEntity [codUsuario=" + codUsuario + ", nombre=" + nombre + ", apeUsuario=" + apeUsuario
				+ ", password=" + password + ", correo=" + correo + ", tipo=" + tipo + "]";
	}
}
