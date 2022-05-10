package com.gnc.task.application.utilities;

import com.gnc.task.application.data.dto.ClienteDTO;
import com.gnc.task.application.data.dto.VehiculoDTO;
import com.gnc.task.application.data.entity.Cliente;
import com.gnc.task.application.data.entity.Vehiculo;
import com.gnc.task.application.data.service.ClientsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientUtils.class);

    public static ClienteDTO newClient(String nombre, String apellido, String dni, String direccion, String telefono, String email, List<VehiculoDTO> vehiculoDTOS) {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setDni(dni);
        cliente.setDireccion(direccion);
        cliente.setTelefono(telefono);
        cliente.setEmail(email);
        return cliente;

    }

    public static List<ClienteDTO> getAllClients(ClientsService clientsService) {
        List<Cliente> clientList = new ArrayList<>();
        clientList = clientsService.getAllClients();
        List<ClienteDTO> clientListDTO = new ArrayList<>();
        if (Objects.nonNull(clientList) && !clientList.isEmpty()) {
            clientList.stream().forEach(clients -> {
                List<Vehiculo> vehiculoList = new ArrayList<>();
                vehiculoList = clients.getVehiculos();
                List<VehiculoDTO> vehiculoDTOList = new ArrayList<>();
                if (Objects.nonNull(vehiculoList) && !vehiculoList.isEmpty()) {

                    vehiculoList.stream().forEach(v -> {
                                VehiculoDTO vDTO = new VehiculoDTO();
                                vDTO.setId(v.getId().longValue());
                                vDTO.setDominio(v.getDominio());
                                vDTO.setMarca(v.getMarca());
                                vDTO.setModelo(v.getModelo());
                                vDTO.setKilometro(v.getKilometro());
                                vDTO.setAño(v.getAño());
                                vDTO.setVehiculoId(v.getId().longValue());
                                vehiculoDTOList.add(vDTO);
                            }

                    );
                }
                ClienteDTO clienteDTO = newClient(clients.getNombre(), clients.getApellido(), clients.getDni(), clients.getDireccion(), clients.getTelefono(), clients.getTelefono(), vehiculoDTOList);
                clienteDTO.setId(clients.getId().longValue());
                clientListDTO.add(clienteDTO);
            });
        }
        return clientListDTO;
    }

}
