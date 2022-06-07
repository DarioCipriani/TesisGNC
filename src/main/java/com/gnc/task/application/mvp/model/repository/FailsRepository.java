package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Falla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FailsRepository extends JpaRepository<Falla, Integer> {
    Falla findByNombre(String nombre);

    Optional<Falla> findById(int fallaId);
}
