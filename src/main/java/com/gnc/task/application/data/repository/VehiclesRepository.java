package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiclesRepository extends JpaRepository<Vehiculo, Integer> {
    Vehiculo findByDominio(String dominio);

    Optional<Vehiculo> findById(int dominioId);

}
