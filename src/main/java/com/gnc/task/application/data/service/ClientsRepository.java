package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientsRepository extends JpaRepository<Cliente, Integer> {
    Cliente findByDni(String dni);

    Cliente findById(String clientId);
}
