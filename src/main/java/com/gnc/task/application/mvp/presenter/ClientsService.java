package com.gnc.task.application.mvp.presenter;

import com.gnc.task.application.mvp.model.entity.Cliente;
import com.gnc.task.application.mvp.model.repository.ClientsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class ClientsService extends CrudService<Cliente, Integer> {

    private ClientsRepository repository;

    public ClientsService(@Autowired ClientsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ClientsRepository getRepository() {
        return repository;
    }

    public List<Cliente> getAllClients() {
        return repository.findAll();
    }

    public boolean deleteClientByClientID(int cId) {
        Optional<Cliente> client = repository.findById(cId);
        if (client.isPresent()) {
            repository.delete(client.get());
            return true;
        }
        return false;
    }

    public Cliente getClientByDni(String dni) {
        return repository.findByDni(dni);
    }

    public Cliente updateClient(Cliente client) {
        return repository.save(client);
    }


}
