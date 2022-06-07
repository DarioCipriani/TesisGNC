package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Oblea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WafersRepository extends JpaRepository<Oblea, Integer> {
    Oblea findByNumeroDeOblea(String numeroDeOblea);

    Optional<Oblea> findById(int numeroDeObleaId);
}
