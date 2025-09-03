package com.example.bdMetro.services;

import com.example.bdMetro.entity.UserTarea;
import com.example.bdMetro.repository.UserTareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserTareaService {

    @Autowired
    private UserTareaRepository userTareaRepository;

    public List<UserTarea> getAllTareas() {
        return userTareaRepository.findAll(); // Nuevo método para obtener todas las tareas
    }

    public List<UserTarea> getTareasByUserCode(String userCode) {
        return userTareaRepository.findByUserCode(userCode);
    }



    public Optional<UserTarea> getUserTareaById(Long id) {
        return userTareaRepository.findById(id);
    }

   /* public UserTarea addUserTarea(UserTarea userTarea) {
        return userTareaRepository.save(userTarea);
    }*/

    public UserTarea addUserTarea(UserTarea userTarea) {
        // Validación opcional: Asegurar que clienteId esté presente
        if (userTarea.getClienteId() == null) {
            throw new IllegalArgumentException("clienteId es requerido");
        }
        return userTareaRepository.save(userTarea);
    }

    public List<UserTarea> getTareasByClienteId(Long clienteId) {
        return userTareaRepository.findByClienteId(clienteId); // Nueva método para tareas por cliente
    }

    /*
    public UserTarea updateUserTarea(Long id, UserTarea userTareaDetails) {
        UserTarea userTarea = userTareaRepository.findById(id).orElseThrow(() -> new RuntimeException("UserTarea no encontrada"));
        userTarea.setTarea(userTareaDetails.getTarea());
        userTarea.setCosto(userTareaDetails.getCosto());
        userTarea.setArea(userTareaDetails.getArea());
        userTarea.setDescripcion(userTareaDetails.getDescripcion());
        userTarea.setDescuento(userTareaDetails.getDescuento());
        userTarea.setTotalCost(userTareaDetails.getTotalCost());
        userTarea.setPais(userTareaDetails.getPais());
        userTarea.setRubro(userTareaDetails.getRubro());
        userTarea.setCategoria(userTareaDetails.getCategoria());
        return userTareaRepository.save(userTarea);
    }*/

    public UserTarea updateUserTarea(Long id, UserTarea userTareaDetails) {
        UserTarea userTarea = userTareaRepository.findById(id).orElseThrow(() -> new RuntimeException("UserTarea no encontrada"));
        userTarea.setTarea(userTareaDetails.getTarea());
        userTarea.setCosto(userTareaDetails.getCosto());
        userTarea.setArea(userTareaDetails.getArea());
        userTarea.setDescripcion(userTareaDetails.getDescripcion());
        userTarea.setDescuento(userTareaDetails.getDescuento());
        userTarea.setTotalCost(userTareaDetails.getTotalCost());
        userTarea.setClienteId(userTareaDetails.getClienteId()); // Actualizar clienteId si es necesario
        userTarea.setPais(userTareaDetails.getPais());
        userTarea.setRubro(userTareaDetails.getRubro());
        userTarea.setCategoria(userTareaDetails.getCategoria());
        return userTareaRepository.save(userTarea);
    }

    public void deleteUserTarea(Long id) {
        userTareaRepository.deleteById(id);
    }
}
