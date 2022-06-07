package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiclesRepository extends JpaRepository<Vehiculo, Integer> {
    Optional<Vehiculo> findByDominio(String dominio);

    Optional<Vehiculo> findById(int dominioId);

}
