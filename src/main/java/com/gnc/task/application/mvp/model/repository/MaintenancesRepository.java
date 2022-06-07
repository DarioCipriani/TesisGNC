package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenancesRepository extends JpaRepository<Mantenimiento, Integer> {
    List<Mantenimiento> findByVehiculo_Dominio(String dominio);

    Optional<Mantenimiento> findById(int mantenimientoId);
}
