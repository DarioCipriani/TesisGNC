package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.PruebaHidraulica;
import com.gnc.task.application.data.repository.HydraulicTestingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class HydraulicTestService extends CrudService<PruebaHidraulica, Integer> {

    private HydraulicTestingRepository repository;

    public HydraulicTestService(@Autowired HydraulicTestingRepository repository) {
        this.repository = repository;
    }

    @Override
    protected HydraulicTestingRepository getRepository() {
        return repository;
    }

    public List<PruebaHidraulica> getAllHydraulicTesting() {
        return repository.findAll();
    }

    public boolean deleteHydraulicTestByHydraulicTestID(int hId) {
        Optional<PruebaHidraulica> ph = repository.findById(hId);
        if (ph.isPresent()) {
            repository.delete(ph.get());
            return true;
        }
        return false;
    }

    public PruebaHidraulica getHydraulicTestByNumeroCertificado(String nuemeroCertificado) {
        return repository.findByNumeroCertificado(nuemeroCertificado);
    }

    public PruebaHidraulica updateHydraulicTest(PruebaHidraulica ph) {
        return repository.save(ph);
    }


}
