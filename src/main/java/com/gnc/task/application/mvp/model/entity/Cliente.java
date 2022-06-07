package com.gnc.task.application.mvp.model.entity;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client_config")

public class Cliente extends AbstractEntity implements Serializable {

    private String nombre;
    private String apellido;
    @Pattern(regexp = "[0-9]+", message="El dni sólo puede tener números")
    @Column(unique = true)
    private String dni;
    @Nonnull
    private String direccion;
    private String email;
    private String telefono;
    private boolean activo;
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<Vehiculo> vehiculos = new ArrayList<>();

    public Cliente() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String name) {
        this.nombre = name;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String surname) {
        this.apellido = surname;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
