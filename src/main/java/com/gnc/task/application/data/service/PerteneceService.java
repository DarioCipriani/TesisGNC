package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Cliente;
import com.gnc.task.application.data.entity.Pertenece;
import com.gnc.task.application.data.repository.ClientsRepository;
import com.gnc.task.application.data.repository.PerteneceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class PerteneceService extends CrudService<Pertenece, Integer> {

    private PerteneceRepository repository;

    public PerteneceService(@Autowired PerteneceRepository repository) {
        this.repository = repository;
    }

    @Override
    protected PerteneceRepository getRepository() {
        return repository;
    }

    public List<Pertenece> getAllPertenece() {
        return repository.findAll();
    }

    public boolean deletePerteneceByPerteneceID(int pId) {
        Optional<Pertenece> pertenece = repository.findById(pId);
        if (pertenece.isPresent()) {
            repository.delete(pertenece.get());
            return true;
        }
        return false;
    }

    public List<Pertenece> getByVehiculo_Dominio(String dominio) {
        return repository.findByVehiculo_Dominio(dominio);
    }
    public List<Pertenece> getByCliente_Dni(String dni) {
        return repository.findByCliente_Dni(dni);
    }
    public Pertenece updatePertenece(Pertenece pertenece) {
        return repository.save(pertenece);
    }


}
