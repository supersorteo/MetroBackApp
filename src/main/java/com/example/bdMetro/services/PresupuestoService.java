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
import com.example.bdMetro.services.MembershipLimitService;

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
    private EmpresaRepository empresaRepository;

    @Autowired
    private UserTareaRepository userTareaRepository;

    @Autowired
    private MembershipLimitService membershipLimitService;

    @Transactional
    public Presupuesto savePresupuesto(Presupuesto presupuesto) {
        if (presupuesto.getName() == null || presupuesto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del presupuesto es requerido");
        }

        List<UserTarea> tareasVerificadas = new ArrayList<>();
        if (presupuesto.getTareas() != null && !presupuesto.getTareas().isEmpty()) {
            for (UserTarea tarea : presupuesto.getTareas()) {
                if (tarea.getId() != null) {
                    UserTarea tareaExistente = userTareaRepository.findById(tarea.getId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "La tarea con ID " + tarea.getId() + " no existe"));
                    tareasVerificadas.add(tareaExistente);
                }
            }
        }
        presupuesto.setTareas(tareasVerificadas);

        // userCode siempre proviene de las tareas verificadas en DB (fuente confiable),
        // nunca del payload — evita que un cliente envíe el código de otro usuario.
        String authorizedUserCode = tareasVerificadas.stream()
                .map(UserTarea::getUserCode)
                .filter(uc -> uc != null && !uc.isBlank())
                .findFirst()
                .orElse(null);

        // Nuevo presupuesto sin tareas verificadas = no hay identidad confiable → rechazar.
        // Evita bypass de límite enviando un presupuesto con lista de tareas vacía.
        if (presupuesto.getId() == null && authorizedUserCode == null) {
            throw new IllegalArgumentException(
                "El presupuesto debe contener al menos una tarea válida.");
        }

        if (authorizedUserCode != null) {
            presupuesto.setUserCode(authorizedUserCode);
            if (presupuesto.getId() == null) {
                membershipLimitService.assertPresupuestoLimitNotReached(authorizedUserCode);
            }
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

        // PASO 1: Actualizar campos básicos
        presupuesto.setName(presupuestoActualizado.getName());
        if (presupuestoActualizado.getCliente() != null) {
            presupuesto.setCliente(presupuestoActualizado.getCliente());
        }
        if (presupuestoActualizado.getEmpresa() != null) {
            presupuesto.setEmpresa(presupuestoActualizado.getEmpresa());
        }

        // PASO 2: Procesar las tareas (REEMPLAZAR COMPLETAMENTE)
        if (presupuestoActualizado.getTareas() != null && !presupuestoActualizado.getTareas().isEmpty()) {
            List<UserTarea> tareasVerificadas = new ArrayList<>();

            for (UserTarea tarea : presupuestoActualizado.getTareas()) {
                if (tarea.getId() != null) {
                    Optional<UserTarea> tareaExistente = userTareaRepository.findById(tarea.getId());

                    if (tareaExistente.isPresent()) {
                        tareasVerificadas.add(tareaExistente.get());
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
