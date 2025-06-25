package com.example.demo.entity;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_chat")
public class ChatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cod_chat", nullable = false, unique = true)
	private Integer codChat;
	
	@Column(name="asunto", nullable = false)
	private String asunto;

	@Column(name = "fec_inicio", nullable = false, columnDefinition = "DATE")
	private LocalDate fecInicio;

	@Column(name = "fec_cierre", nullable = false, columnDefinition = "DATE")
	private LocalDate fecCierre;

	@Column(name = "estado", nullable = false)
	private boolean estado;

	@ManyToOne
	@JoinColumn(name = "cod_usuario", nullable = false)
	private CuentaEntity cuenta;

	@ManyToOne
	@JoinColumn(name = "cod_cat", nullable = false)
	private CategoriaEntity categoria;

	@ManyToOne
	@JoinColumn(name = "cod_prioridad", nullable = false)
	private PrioridadEntity prioridad;

	public Integer getCodChat() {
		return codChat;
	}

	public void setCodChat(Integer codChat) {
		this.codChat = codChat;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public LocalDate getFecInicio() {
		return fecInicio;
	}

	public void setFecInicio(LocalDate fecInicio) {
		this.fecInicio = fecInicio;
	}

	public LocalDate getFecCierre() {
		return fecCierre;
	}

	public void setFecCierre(LocalDate fecCierre) {
		this.fecCierre = fecCierre;
	}

	public boolean isEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public CuentaEntity getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaEntity cuenta) {
		this.cuenta = cuenta;
	}

	public CategoriaEntity getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaEntity categoria) {
		this.categoria = categoria;
	}

	public PrioridadEntity getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(PrioridadEntity prioridad) {
		this.prioridad = prioridad;
	}

	public ChatEntity(Integer codChat, String asunto, LocalDate fecInicio, LocalDate fecCierre, boolean estado,
			CuentaEntity cuenta, CategoriaEntity categoria, PrioridadEntity prioridad) {
		super();
		this.codChat = codChat;
		this.asunto = asunto;
		this.fecInicio = fecInicio;
		this.fecCierre = fecCierre;
		this.estado = estado;
		this.cuenta = cuenta;
		this.categoria = categoria;
		this.prioridad = prioridad;
	}

	public ChatEntity() {}

	@Override
	public String toString() {
		return "ChatEntity [codChat=" + codChat + ", asunto=" + asunto + ", fecInicio=" + fecInicio + ", fecCierre="
				+ fecCierre + ", estado=" + estado + ", cuenta=" + cuenta + ", categoria=" + categoria + ", prioridad="
				+ prioridad + "]";
	}
	
}
