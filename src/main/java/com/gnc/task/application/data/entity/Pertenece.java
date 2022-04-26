package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "pertenece")

public class Pertenece extends AbstractEntity implements Serializable {

	@ManyToOne
	private Cliente cliente;
	@ManyToOne
	private Vehiculo vehiculo;

	private boolean pertenecen = true;

	public Pertenece() {
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public boolean getPertenecen() {
		return pertenecen;
	}

	public void setPertenecen(boolean pertenecen) {
		this.pertenecen = pertenecen;
	}
}
