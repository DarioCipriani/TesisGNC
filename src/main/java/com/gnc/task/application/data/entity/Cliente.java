package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "client_config")

public class Cliente extends AbstractEntity implements Serializable {

	private String nombre;
	private String apellido;
	@Column(unique = true)
	private String dni;
	private String direccion;
	private String email;
	private String telefono;

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
}
