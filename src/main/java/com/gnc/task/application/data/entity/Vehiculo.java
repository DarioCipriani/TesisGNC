package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigInteger;

@Entity
@Table(name = "vehiculo_config")

public class Vehiculo extends AbstractEntity implements Serializable {

	@Column(unique = true)
	private String dominio;
	private String marca;
	private String modelo;
	private Integer kilometro;
	private String año;

	public Vehiculo() {
	}

	public String getDominio() {
		return dominio;
	}

	public void setDominio(String dominio) {
		this.dominio = dominio.toUpperCase();
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Integer getKilometro() {
		return kilometro;
	}

	public void setKilometro(Integer kilometro) {
		this.kilometro = kilometro;
	}

	public String getAño() {
		return año;
	}

	public void setAño(String año) {
		this.año = año;
	}

	@Override
	public String toString() {
		return dominio;
	}
}
