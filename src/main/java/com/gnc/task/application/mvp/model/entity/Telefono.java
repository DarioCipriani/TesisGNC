package com.gnc.task.application.mvp.model.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "telefono")

public class Telefono extends AbstractEntity {

    private String prefijo;
    private String numero;

    public Telefono(String prefijo, String numero) {
        this.prefijo = prefijo;
        this.numero = numero;
    }

    public Telefono() {

    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "(" + prefijo + ")-" + numero;
    }
}
