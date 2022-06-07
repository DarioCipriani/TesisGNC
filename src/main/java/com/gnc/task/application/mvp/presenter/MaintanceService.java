package com.gnc.task.application.mvp.presenter;

import com.gnc.task.application.mvp.model.entity.Mantenimiento;
import com.gnc.task.application.mvp.model.repository.MaintenancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class MaintanceService extends CrudService<Mantenimiento, Integer> {

    private MaintenancesRepository repository;

    public MaintanceService(@Autowired MaintenancesRepository repository) {
        this.repository = repository;
    }

    @Override
    protected MaintenancesRepository getRepository() {
        return repository;
    }

    public List<Mantenimiento> getAllMantenimientos() {
        return repository.findAll();
    }

    public boolean deleteMantenimientoByMantenimientoID(int mId) {
        Optional<Mantenimiento> mantenimiento = repository.findById(mId);
        if (mantenimiento.isPresent()) {
            repository.delete(mantenimiento.get());
            return true;
        }
        return false;
    }

    public List<Mantenimiento> getMantenimientoByDominio(String dominio) {
        return repository.findByVehiculo_Dominio(dominio);
    }

    public Mantenimiento getMantenimientoById(int id) {
        return (repository.findById(id)).isPresent() ? repository.findById(id).get() : null;
    }

    public Mantenimiento updateMantenimiento(Mantenimiento mantenimiento) {
        return repository.save(mantenimiento);
    }


}
