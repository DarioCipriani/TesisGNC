package com.gnc.task.application.mvp.presenter;

import com.gnc.task.application.mvp.model.entity.Cliente;
import com.gnc.task.application.mvp.model.entity.Vehiculo;
import com.gnc.task.application.mvp.model.repository.ClientsRepository;
import com.gnc.task.application.mvp.model.repository.VehiclesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService extends CrudService<Vehiculo, Integer> {

    private VehiclesRepository repository;
    private ClientsRepository clientsRepository;

    public VehicleService(@Autowired VehiclesRepository repository, @Autowired ClientsRepository clientsRepository) {
        this.repository = repository;
        this.clientsRepository=clientsRepository;
    }

    @Override
    protected VehiclesRepository getRepository() {
        return repository;
    }

    public List<Vehiculo> getAllVehiculos() {
        return repository.findAll();
    }

    public List<Cliente> getAllClientsByDominio(String dominio) {
        List<Cliente> clientes = new ArrayList<>();
        Optional<Vehiculo> v = repository.findByDominio(dominio);
        if (v.isPresent()) {
            clientes = v.get().getClientes();
        }
        return clientes;
    }

    public boolean deleteVehiculoByVehiculoID(int vId) {
        Optional<Vehiculo> vehiculo = repository.findById(vId);
        if (vehiculo.isPresent()) {
            repository.deleteById(vehiculo.get().getId());
            return true;
        }
        return false;
    }

    public boolean deleteVehiculoByDominio(String dominio) {
        Optional<Vehiculo> vehiculo = repository.findByDominio(dominio);
        if (vehiculo.isPresent()) {
            List<Cliente> cliente = vehiculo.get().getClientes();
            List<Vehiculo> vehiculoList = new ArrayList<>();
            cliente.forEach(c -> {
                c.getVehiculos().forEach(v -> {
                    if (!dominio.equals(v.getDominio())) {
                        vehiculoList.add(v);
                    }
                });
                c.setVehiculos(vehiculoList);
                clientsRepository.save(c);
            });
            repository.delete(vehiculo.get());
            return true;
        }
        return false;
    }

    public Vehiculo getVehiculoByDominio(String dominio) {
        Optional<Vehiculo> vehiculo = repository.findByDominio(dominio);
        if (vehiculo.isPresent()) {
            return vehiculo.get();
        }else
            return null;
    }

    public Vehiculo updateVehiculo(Vehiculo vehiculo) {
        return repository.save(vehiculo);
    }


}
