package com.gnc.task.application.data.repository;

import com.gnc.task.application.data.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientsRepository extends JpaRepository<Cliente, Integer> {
    Cliente findByDni(String dni);
    Cliente findById(String clientId);


}
