package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Producto, Integer> {
    Producto findByCodigo(String codigo);

    Optional<Producto> findById(int productId);
}
