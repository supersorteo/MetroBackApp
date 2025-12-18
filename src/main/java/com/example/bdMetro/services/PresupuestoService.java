package com.example.bdMetro.services;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.entity.Presupuesto;
import com.example.bdMetro.entity.UserTarea;
import com.example.bdMetro.repository.ClienteRepository;
import com.example.bdMetro.repository.EmpresaRepository;
import com.example.bdMetro.repository.PresupuestoRepository;
import com.example.bdMetro.repository.UserTareaRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoService {
    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UserTareaRepository userTareaRepository;

    @Transactional
    public Presupuesto savePresupuesto(Presupuesto presupuesto) {
        // Validación 1: Cliente requerido
        if (presupuesto.getCliente() == null || presupuesto.getCliente().getId() == null) {
            throw new IllegalArgumentException("El presupuesto debe tener un cliente asociado");
        }

        // Validación 2: Nombre requerido
        if (presupuesto.getName() == null || presupuesto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del presupuesto es requerido");
        }

        // Validación 3: Procesar tareas
        if (presupuesto.getTareas() != null && !presupuesto.getTareas().isEmpty()) {
            List<UserTarea> tareasVerificadas = new ArrayList<>();

            for (UserTarea tarea : presupuesto.getTareas()) {
                if (tarea.getId() != null) {
                    Optional<UserTarea> tareaExistente = userTareaRepository.findById(tarea.getId());

                    if (tareaExistente.isPresent()) {
                        UserTarea tareaEnBD = tareaExistente.get();

                        // Verificar que la tarea pertenece al cliente
                        if (!tareaEnBD.getClienteId().equals(presupuesto.getCliente().getId())) {
                            throw new IllegalArgumentException(
                                    "La tarea " + tarea.getId() + " no pertenece a este cliente"
                            );
                        }

                        tareasVerificadas.add(tareaEnBD);
                    } else {
                        throw new IllegalArgumentException(
                                "La tarea con ID " + tarea.getId() + " no existe"
                        );
                    }
                }
            }

            presupuesto.setTareas(tareasVerificadas);
        } else {
            presupuesto.setTareas(new ArrayList<>());
        }

        return presupuestoRepository.save(presupuesto);
    }



    public List<Presupuesto> getPresupuestosByClienteId(Long clienteId) {
        return presupuestoRepository.findByClienteIdWithTareas(clienteId);
    }


    public List<Presupuesto> getAllPresupuestosByUserCode(String userCode) {
        return presupuestoRepository.findByUserCode(userCode);
    }


    public Presupuesto getPresupuestoById(Long id) {
        return presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
    }


    @Transactional
    public void deletePresupuesto(Long id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // 🔥 CON @ManyToMany: Hibernate se encarga automáticamente de eliminar
        // las filas en la tabla intermedia presupuesto_tareas
        // Las tareas NO se eliminan, solo se desvinculan
        presupuestoRepository.delete(presupuesto);
    }

    public List<Presupuesto> getAllPresupuestos() {
        return presupuestoRepository.findAll();
    }


    @Transactional
    public Presupuesto addTareaToPresupuesto(Long presupuestoId, Long tareaId) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        UserTarea tarea = userTareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (!tarea.getClienteId().equals(presupuesto.getCliente().getId())) {
            throw new IllegalArgumentException("La tarea no pertenece a este cliente");
        }

        if (!presupuesto.getTareas().contains(tarea)) {
            presupuesto.addTarea(tarea);
            presupuestoRepository.save(presupuesto);
        }

        return presupuesto;
    }

    // 🔥 MÉTODO NUEVO: Remover tarea de presupuesto
    @Transactional
    public Presupuesto removeTareaFromPresupuesto(Long presupuestoId, Long tareaId) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        UserTarea tarea = userTareaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        presupuesto.removeTarea(tarea);
        return presupuestoRepository.save(presupuesto);
    }




    @Transactional
    public Presupuesto updatePresupuesto(Long id, Presupuesto presupuestoActualizado) {
        // Validación 1: Presupuesto debe existir
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // Validación 2: Nombre requerido
        if (presupuestoActualizado.getName() == null ||
                presupuestoActualizado.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del presupuesto es requerido");
        }

        // Validación 3: Cliente requerido
        if (presupuestoActualizado.getCliente() == null ||
                presupuestoActualizado.getCliente().getId() == null) {
            throw new IllegalArgumentException("El cliente es requerido");
        }

        // PASO 1: Actualizar campos básicos
        presupuesto.setName(presupuestoActualizado.getName());
        presupuesto.setCliente(presupuestoActualizado.getCliente());

        // PASO 2: Procesar las tareas (REEMPLAZAR COMPLETAMENTE)
        if (presupuestoActualizado.getTareas() != null && !presupuestoActualizado.getTareas().isEmpty()) {
            List<UserTarea> tareasVerificadas = new ArrayList<>();

            for (UserTarea tarea : presupuestoActualizado.getTareas()) {
                if (tarea.getId() != null) {
                    Optional<UserTarea> tareaExistente = userTareaRepository.findById(tarea.getId());

                    if (tareaExistente.isPresent()) {
                        UserTarea tareaEnBD = tareaExistente.get();

                        // Verificar que la tarea pertenece al cliente
                        if (!tareaEnBD.getClienteId().equals(presupuestoActualizado.getCliente().getId())) {
                            throw new IllegalArgumentException(
                                    "La tarea " + tarea.getId() + " no pertenece a este cliente"
                            );
                        }

                        tareasVerificadas.add(tareaEnBD);
                    } else {
                        throw new IllegalArgumentException(
                                "La tarea con ID " + tarea.getId() + " no existe"
                        );
                    }
                }
            }

            // Reemplazar completamente la lista de tareas
            presupuesto.setTareas(tareasVerificadas);
        } else {
            presupuesto.setTareas(new ArrayList<>());
        }

        // PASO 3: Guardar y devolver
        return presupuestoRepository.save(presupuesto);
    }


}
