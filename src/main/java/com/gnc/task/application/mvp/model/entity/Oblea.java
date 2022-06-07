package com.gnc.task.application.mvp.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "oblea_config")

public class Oblea extends AbstractEntity implements Serializable {

    @Column(unique = true)
    private String numeroDeOblea;
    private String fechaVencimiento;
    private String obleaVigente;

    public Oblea() {
    }

    public String getNumeroDeOblea() {
        return numeroDeOblea;
    }

    public void setNumeroDeOblea(String numeroDeOblea) {
        this.numeroDeOblea = numeroDeOblea;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getObleaVigente() {
        return obleaVigente;
    }

    public void setObleaVigente(String obleaVigente) {
        this.obleaVigente = obleaVigente;
    }
}
