package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetsRepository extends JpaRepository<Presupuesto, Integer> {
    Presupuesto findBynroPresupuesto(Integer nroPresupuesto);

    Optional<Presupuesto> findById(int budgetId);
}
