package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.Oblea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WafersRepository extends JpaRepository<Oblea, Integer> {
    Oblea findByNumeroDeOblea(String numeroDeOblea);

    Optional<Oblea> findById(int numeroDeObleaId);
}
