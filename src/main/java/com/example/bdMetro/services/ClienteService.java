package com.example.bdMetro.services;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Presupuesto;
import com.example.bdMetro.entity.UserTarea;
import com.example.bdMetro.repository.ClienteRepository;
import com.example.bdMetro.repository.EmpresaRepository;
import com.example.bdMetro.repository.PresupuestoRepository;
import com.example.bdMetro.repository.UserTareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private UserTareaRepository userTareaRepository;

    public List<Cliente> getClienteByUserCode(String userCode) {
        return clienteRepository.findByUserCode(userCode);
    }

    public Cliente saveCliente(Cliente cliente) {
        if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido para guardar el cliente");
        }
        if (cliente.getEmpresaId() == null) {
            throw new IllegalArgumentException("El empresaId es requerido para guardar el cliente");
        }
        if (!empresaRepository.existsById(cliente.getEmpresaId())) {
            throw new IllegalArgumentException("No existe una empresa con el ID: " + cliente.getEmpresaId());
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

    public List<Cliente> getClientesByEmpresaId(Long empresaId) {
        return clienteRepository.findByEmpresaId(empresaId);
    }

    public Cliente updateCliente(Long id, Cliente clienteDetails) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        if (clienteDetails.getEmpresaId() == null) {
            throw new IllegalArgumentException("El empresaId es requerido para actualizar el cliente");
        }
        if (!empresaRepository.existsById(clienteDetails.getEmpresaId())) {
            throw new IllegalArgumentException("No existe una empresa con el ID: " + clienteDetails.getEmpresaId());
        }
        cliente.setName(clienteDetails.getName());
        cliente.setContact(clienteDetails.getContact());
        cliente.setBudgetDate(clienteDetails.getBudgetDate());
        cliente.setAdditionalDetails(clienteDetails.getAdditionalDetails());
        cliente.setEmail(clienteDetails.getEmail());
        cliente.setClave(clienteDetails.getClave());
        cliente.setDireccion(clienteDetails.getDireccion());
        cliente.setEmpresaId(clienteDetails.getEmpresaId());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deleteCliente(Long id) {
        // 1. Eliminar todos los presupuestos del cliente
        //    Hibernate elimina automáticamente las filas de la tabla intermedia presupuesto_tareas
        List<Presupuesto> presupuestos = presupuestoRepository.findByClienteId(id);
        presupuestoRepository.deleteAll(presupuestos);

        // 2. Soft-delete de todas las user-tareas del cliente
        List<UserTarea> tareas = userTareaRepository.findByClienteId(id);
        tareas.forEach(t -> t.setDeleted(true));
        if (!tareas.isEmpty()) {
            userTareaRepository.saveAll(tareas);
        }

        // 3. Eliminar el cliente
        clienteRepository.deleteById(id);
    }

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }
}
