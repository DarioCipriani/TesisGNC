package com.gnc.task.application.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gnc.task.application.data.AbstractEntity;
import com.vaadin.fusion.Nonnull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "falla")
public class Fallas  extends AbstractEntity implements Serializable {


	@Nonnull
	private String marca;
	private String modelo;
	@Nonnull
	private String description;
	private String solution;

	public Fallas() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public String getSolution() {
		return this.solution ;
	}
}
