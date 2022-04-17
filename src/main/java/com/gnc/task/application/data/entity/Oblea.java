package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "oblea_config")

public class Oblea extends AbstractEntity implements Serializable {

	@Column(unique = true)
	private String numeroDeOblea;
	private Date fechaVencimiento;
	private Boolean obleaVigente;

	public Oblea() {
	}

	public String getNumeroDeOblea() {
		return numeroDeOblea;
	}

	public void setNumeroDeOblea(String numeroDeOblea) {
		this.numeroDeOblea = numeroDeOblea;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Boolean getObleaVigente() {
		return obleaVigente;
	}

	public void setObleaVigente(Boolean obleaVigente) {
		this.obleaVigente = obleaVigente;
	}
}
