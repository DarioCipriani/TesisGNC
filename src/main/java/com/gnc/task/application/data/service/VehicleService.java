package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Vehiculo;
import com.gnc.task.application.data.repository.VehiclesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService extends CrudService<Vehiculo, Integer> {

    private VehiclesRepository repository;

    public VehicleService(@Autowired VehiclesRepository repository) {
        this.repository = repository;
    }

    @Override
    protected VehiclesRepository getRepository() {
        return repository;
    }

    public List<Vehiculo> getAllVehiculos() {
        return repository.findAll();
    }

    public boolean deleteVehiculoByVehiculoID(int vId) {
        Optional<Vehiculo> vehiculo = repository.findById(vId);
        if (vehiculo.isPresent()) {
            repository.delete(vehiculo.get());
            return true;
        }
        return false;
    }

    public Vehiculo getVehiculoByByDominio(String dominio) {
        return repository.findByDominio(dominio);
    }

    public Vehiculo updateVehiculo(Vehiculo vehiculo) {
        return repository.save(vehiculo);
    }


}
