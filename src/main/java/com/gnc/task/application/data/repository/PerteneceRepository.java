package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.Pertenece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerteneceRepository extends JpaRepository<Pertenece, Integer> {
    List<Pertenece> findByCliente_Dni(String dni);
    List<Pertenece> findByVehiculo_Dominio(String dominio);
}
