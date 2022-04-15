package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Fallas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FallaRepository extends JpaRepository<Fallas, Integer> {
    Fallas findByMarca(String marca);

    Fallas findByModelo(String modelo);

    Fallas findBySolution(String solution);

    Fallas findByDescription(String description);

    Fallas findById(String cId);
}
