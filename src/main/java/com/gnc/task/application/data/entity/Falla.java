package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "fail_config")

public class Falla extends AbstractEntity implements Serializable {

	@Column(unique = true)
	private String nombre;
	private String marcaVehiculo;
	private String modeloVehiculo;
	private String añoVehiculo;
	private String descripcionFalla;
	private String descripcionSolucion;

	public Falla() {
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMarcaVehiculo() {
		return marcaVehiculo;
	}

	public void setMarcaVehiculo(String marcaVehiculo) {
		this.marcaVehiculo = marcaVehiculo;
	}

	public String getModeloVehiculo() {
		return modeloVehiculo;
	}

	public void setModeloVehiculo(String modeloVehiculo) {
		this.modeloVehiculo = modeloVehiculo;
	}

	public String getAñoVehiculo() {
		return añoVehiculo;
	}

	public void setAñoVehiculo(String añoVehiculo) {
		this.añoVehiculo = añoVehiculo;
	}

	public String getDescripcionFalla() {
		return descripcionFalla;
	}

	public void setDescripcionFalla(String descripcionFalla) {
		this.descripcionFalla = descripcionFalla;
	}

	public String getDescripcionSolucion() {
		return descripcionSolucion;
	}

	public void setDescripcionSolucion(String descripcionSolucion) {
		this.descripcionSolucion = descripcionSolucion;
	}
}
