package com.gnc.task.application.data.dto;

import javax.validation.constraints.NotNull;

public class VehiculoDTO {
    private Long id;
    @NotNull
    private String dominio;
    private String marca;
    private String modelo;
    private Integer kilometro;
    private String año;
    private Long vehiculoId;
    private Long clienteId;

    public VehiculoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
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

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
