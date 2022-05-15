package com.gnc.task.application.data.dto;

import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

public class ClienteDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String direccion;
    private String telefono;
    private String email;
    private List<VehiculoDTO> vehiculoDTOS= new ArrayList<>();

    public ClienteDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<VehiculoDTO> getVehiculoDTOS() {
        return vehiculoDTOS;
    }

    public void setVehiculoDTOS(List<VehiculoDTO> vehiculoDTOS) {
        this.vehiculoDTOS = vehiculoDTOS;
    }
}
