package com.example.bdMetro.services;

import com.example.bdMetro.entity.UserTarea;
import com.example.bdMetro.repository.UserTareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserTareaService {

    @Autowired
    private UserTareaRepository userTareaRepository;

    public List<UserTarea> getAllTareas() {
        return userTareaRepository.findAllActive();
    }

    public List<UserTarea> getTareasByUserCode(String userCode) {
        return userTareaRepository.findByUserCode(userCode);
    }




    public Optional<UserTarea> getUserTareaById(Long id) {
        return userTareaRepository.findById(id);
    }



    public UserTarea addUserTarea(UserTarea userTarea) {
        if (userTarea.getClienteId() == null) {
            throw new IllegalArgumentException("clienteId es requerido");
        }
        userTarea.setDeleted(false);
        return userTareaRepository.save(userTarea);
    }


    public List<UserTarea> getTareasByClienteId(Long clienteId) {
        return userTareaRepository.findByClienteIdAndDeletedFalse(clienteId);
    }

    public List<UserTarea> getTareasByClienteAndEmpresa(Long clienteId, Long empresaId) {
        return userTareaRepository.findByClienteIdAndEmpresaIdOrNullAndDeletedFalse(clienteId, empresaId);
    }


    public UserTarea updateUserTarea(Long id, UserTarea userTareaDetails) {
        UserTarea userTarea = userTareaRepository.findById(id).orElseThrow(() -> new RuntimeException("UserTarea no encontrada"));
        userTarea.setTarea(userTareaDetails.getTarea());
        userTarea.setCosto(userTareaDetails.getCosto());
        userTarea.setArea(userTareaDetails.getArea());
        userTarea.setDescripcion(userTareaDetails.getDescripcion());
        userTarea.setDescuento(userTareaDetails.getDescuento());
        userTarea.setTotalCost(userTareaDetails.getTotalCost());
        userTarea.setClienteId(userTareaDetails.getClienteId());
        userTarea.setEmpresaId(userTareaDetails.getEmpresaId());
        userTarea.setPais(userTareaDetails.getPais());
        userTarea.setRubro(userTareaDetails.getRubro());
        userTarea.setCategoria(userTareaDetails.getCategoria());
        return userTareaRepository.save(userTarea);
    }

    @Transactional
    public void deleteAllTareasByClienteId(Long clienteId) {
        userTareaRepository.softDeleteAllByClienteId(clienteId);
    }

    @Transactional
    public void deleteAllTareasByClienteAndEmpresa(Long clienteId, Long empresaId) {
        userTareaRepository.softDeleteAllByClienteIdAndEmpresaId(clienteId, empresaId);
    }

    @Transactional
    public void deleteUserTarea(Long id) {
        UserTarea tarea = userTareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        tarea.setDeleted(true);
        userTareaRepository.save(tarea);
    }

}
