package com.gnc.task.application.mvp.presenter;

import com.gnc.task.application.mvp.model.entity.Oblea;
import com.gnc.task.application.mvp.model.repository.WafersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class WaferService extends CrudService<Oblea, Integer> {

    private WafersRepository repository;

    public WaferService(@Autowired WafersRepository repository) {
        this.repository = repository;
    }

    @Override
    protected WafersRepository getRepository() {
        return repository;
    }

    public List<Oblea> getAllWafers() {
        return repository.findAll();
    }

    public boolean deleteWaferByWaferID(int wId) {
        Optional<Oblea> oblea = repository.findById(wId);
        if (oblea.isPresent()) {
            repository.delete(oblea.get());
            return true;
        }
        return false;
    }

    public Oblea getWaferByNumeroDeOblea(String numeroDeOblea) {
        return repository.findByNumeroDeOblea(numeroDeOblea);
    }

    public Oblea updateWafer(Oblea oblea) {
        return repository.save(oblea);
    }

}
