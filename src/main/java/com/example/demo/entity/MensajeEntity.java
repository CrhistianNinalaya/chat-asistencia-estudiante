package com.example.demo.entity;



import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;



@Entity
@Table(name="tb_mensaje")
public class MensajeEntity {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cod_mensaje", nullable = false, unique = true)
	private Integer codMensaje;
	
	
	@Column(name="contenido", nullable = false)
	private String contenido;
	
	@Column(name="fec_mensaje", nullable = false, columnDefinition = "DATETIME")
	private LocalDateTime fecMensaje;
	
	@ManyToOne
	@JoinColumn(name="cod_chat", nullable= false)
	private ChatEntity chat;
	
	@ManyToOne
	@JoinColumn(name = "cod_usuario", nullable = false)
	private CuentaEntity cuenta;

	public Integer getCodMensaje() {
		return codMensaje;
	}

	public void setCodMensaje(Integer codMensaje) {
		this.codMensaje = codMensaje;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public LocalDateTime getFecMensaje() {
		return fecMensaje;
	}

	public void setFecMensaje(LocalDateTime fecMensaje) {
		this.fecMensaje = fecMensaje;
	}

	public ChatEntity getChat() {
		return chat;
	}

	public void setChat(ChatEntity chat) {
		this.chat = chat;
	}

	public CuentaEntity getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaEntity cuenta) {
		this.cuenta = cuenta;
	}

	public MensajeEntity(Integer codMensaje, String contenido, LocalDateTime fecMensaje, ChatEntity chat,
			CuentaEntity cuenta) {
		this.codMensaje = codMensaje;
		this.contenido = contenido;
		this.fecMensaje = fecMensaje;
		this.chat = chat;
		this.cuenta = cuenta;
	}

	public MensajeEntity() {
	}

	@Override
	public String toString() {
		return "MensajeEntity [codMensaje=" + codMensaje + ", contenido=" + contenido + ", fecMensaje=" + fecMensaje
				+ ", chat=" + chat + ", cuenta=" + cuenta + "]";
	}
}
