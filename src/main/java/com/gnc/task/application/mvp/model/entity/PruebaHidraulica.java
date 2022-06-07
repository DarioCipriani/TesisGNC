package com.gnc.task.application.mvp.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "pruebaHidraulica_config")

public class PruebaHidraulica extends AbstractEntity implements Serializable {

    @Column(unique = true)
    private String numeroCertificado;
    private String pasoPrueba;
    @Lob
    private String descripcion;
    private String fechaVencimientoPH;

    public PruebaHidraulica() {
    }

    public String getNumeroCertificado() {
        return numeroCertificado;
    }

    public void setNumeroCertificado(String numeroCertificado) {
        this.numeroCertificado = numeroCertificado;
    }

    public String getPasoPrueba() {
        return pasoPrueba;
    }

    public void setPasoPrueba(String pasoPrueba) {
        this.pasoPrueba = pasoPrueba;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaVencimientoPH() {
        return fechaVencimientoPH;
    }

    public void setFechaVencimientoPH(String fechaVencimientoPH) {
        this.fechaVencimientoPH = fechaVencimientoPH;
    }
}
