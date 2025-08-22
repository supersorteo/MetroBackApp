package com.example.bdMetro.services;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;


    public List<Cliente> getClienteByUserCode(String userCode) {
        return clienteRepository.findByUserCode(userCode); // Changed to return List
    }


    /*
    public Cliente saveCliente(Cliente cliente) {
        Cliente existingCliente = clienteRepository.findByUserCodeAndName(cliente.getUserCode(), cliente.getName());
        if (existingCliente != null) {
            throw new IllegalArgumentException("Este cliente ya está registrado con el nombre '" + cliente.getName() + "' para el código de usuario '" + cliente.getUserCode() + "'");
        }
        return clienteRepository.save(cliente);
    }*/

    /*
    public Cliente saveCliente(Cliente cliente) {
        if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido para guardar el cliente");
        }
        Cliente existingCliente = clienteRepository.findByEmail(cliente.getEmail());
        if (existingCliente != null) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el email '" + cliente.getEmail() + "'");
        }
        return clienteRepository.save(cliente);
    }*/

    public Cliente saveCliente(Cliente cliente) {
        if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido para guardar el cliente");
        }
        List<Cliente> existingClientes = clienteRepository.findByEmail(cliente.getEmail());
        if (!existingClientes.isEmpty()) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el email '" + cliente.getEmail() + "'");
        }
        return clienteRepository.save(cliente);
    }


    public Optional<Cliente> getClienteById(Long id) {
        return clienteRepository.findById(id);
    }



    public Cliente updateCliente(Long id, Cliente clienteDetails) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        cliente.setName(clienteDetails.getName());
        cliente.setContact(clienteDetails.getContact());
        cliente.setBudgetDate(clienteDetails.getBudgetDate());
        cliente.setAdditionalDetails(clienteDetails.getAdditionalDetails());
        cliente.setEmail(clienteDetails.getEmail());
        cliente.setClave(clienteDetails.getClave());
        cliente.setDireccion(clienteDetails.getDireccion());
        return clienteRepository.save(cliente);
    }

    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

}
