package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.PruebaHidraulica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HydraulicTestingRepository extends JpaRepository<PruebaHidraulica, Integer> {
    PruebaHidraulica findByNumeroCertificado(String numeroCertificado);

    Optional<PruebaHidraulica> findById(int pruebaHidraulicaId);
}
