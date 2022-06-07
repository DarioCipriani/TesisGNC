package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientsRepository extends JpaRepository<Cliente, Integer> {
    Cliente findByDni(String dni);

    Cliente findById(String clientId);


}
