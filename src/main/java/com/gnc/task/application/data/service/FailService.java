package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Falla;
import com.gnc.task.application.data.repository.FailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class FailService extends CrudService<Falla, Integer> {

    private FailsRepository repository;

    public FailService(@Autowired FailsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected FailsRepository getRepository() {
        return repository;
    }

    public List<Falla> getAllFails() {
        return repository.findAll();
    }

    public boolean deleteFailByFailID(int fId) {
        Optional<Falla> falla = repository.findById(fId);
        if (falla.isPresent()) {
            repository.delete(falla.get());
            return true;
        }
        return false;
    }

    public Falla getFallaByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }

    public Falla updateFalla(Falla falla) {
        return repository.save(falla);
    }


}
