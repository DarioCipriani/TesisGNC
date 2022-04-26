package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "presupuesto_config")

public class Presupuesto extends AbstractEntity implements Serializable {

	@Column(unique = true)
	private Integer nroPresupuesto;
	private BigDecimal importeTotal;

	public Presupuesto() {
	}

	public Integer getNroPresupuesto() {
		return nroPresupuesto;
	}

	public void setNroPresupuesto(Integer nroPresupuesto) {
		this.nroPresupuesto = nroPresupuesto;
	}

	public BigDecimal getImporteTotal() {
		return importeTotal;
	}

	public void setImporteTotal(BigDecimal importeTotal) {
		this.importeTotal = importeTotal;
	}
}
