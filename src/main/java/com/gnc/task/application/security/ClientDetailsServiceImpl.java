package com.gnc.task.application.security;

import com.gnc.task.application.data.entity.Cliente;
import com.gnc.task.application.data.service.ClientsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientDetailsServiceImpl implements ClientDetailsService {

    @Autowired
    private ClientsRepository clientRepository;

    public boolean deleteByClientDni(String dni) {
        Cliente client = clientRepository.findByDni(dni);
        clientRepository.delete(client);
        return true;
    }

    public void updateClient(Cliente client) {
        clientRepository.save(client);
    }

    public Optional<Cliente> getClientById(Integer id) {
        return clientRepository.findById(id);
    }

    public List<Cliente> findAllClient() {
        return clientRepository.findAll();
    }

    @Override
    public ClientDetails loadClientByClientId(String s) throws ClientRegistrationException {
        return (ClientDetails) clientRepository.findAll();
    }
}
