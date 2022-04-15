package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Fallas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class FallaService extends CrudService<Fallas, Integer> {

    private FallaRepository repository;

    public FallaService(@Autowired FallaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected FallaRepository getRepository() {
        return repository;
    }

    public List<Fallas> getAllFallas() {
        return repository.findAll();
    }

    public boolean deleteFallaByID(int cId) {
        Optional<Fallas> fallas = repository.findById(cId);
        if (fallas.isPresent()) {
            repository.delete(fallas.get());
            return true;
        }
        return false;
    }

    public Fallas getClientByMarca(String name) {
        return repository.findByMarca(name);
    }

    public Fallas updateFalla(Fallas falla) {
        return repository.save(falla);
    }


}
