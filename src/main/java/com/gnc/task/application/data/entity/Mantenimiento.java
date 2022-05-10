package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;
import com.gnc.task.application.data.TMantenimiento;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "maintenance_config")

public class Mantenimiento extends AbstractEntity implements Serializable {


	private TMantenimiento tipoMantenimiento;
	@Lob
	private String descripcion;
	@ManyToOne(optional = false)
	private Vehiculo vehiculo;

	public Mantenimiento() {
	}

	public TMantenimiento getTipoMantenimiento() {
		return tipoMantenimiento;
	}

	public void setTipoMantenimiento(TMantenimiento tipoMantenimiento) {
		this.tipoMantenimiento = tipoMantenimiento;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}
}
