package com.example.bdMetro.controller;

import com.example.bdMetro.entity.UserTarea;
import com.example.bdMetro.services.UserTareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-tareas")
//@CrossOrigin(value = "http://localhost:4200")
public class UserTareaController {

    @Autowired
    private UserTareaService userTareaService;


    @GetMapping
    public ResponseEntity<?> getAllTareas() {
        List<UserTarea> tareas = userTareaService.getAllTareas();
        if (tareas.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se encontraron tareas");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(tareas);
    }


    @GetMapping("/by-user/{userCode}")
    public List<UserTarea> getTareasByUserCode(@PathVariable String userCode) {
        return userTareaService.getTareasByUserCode(userCode);
    }

    @GetMapping("/by-cliente/{clienteId}")
    public List<UserTarea> getTareasByClienteId(@PathVariable Long clienteId) {
        return userTareaService.getTareasByClienteId(clienteId); // Nuevo endpoint para tareas por cliente
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserTarea> getUserTareaById(@PathVariable Long id) {
        Optional<UserTarea> userTarea = userTareaService.getUserTareaById(id);
        return userTarea.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserTarea addUserTarea(@RequestBody UserTarea userTarea) {
        return userTareaService.addUserTarea(userTarea);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserTarea> updateUserTarea(@PathVariable Long id, @RequestBody UserTarea userTareaDetails) {
        UserTarea updatedUserTarea = userTareaService.updateUserTarea(id, userTareaDetails);
        return ResponseEntity.ok(updatedUserTarea);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserTarea(@PathVariable Long id) {
        userTareaService.deleteUserTarea(id);
        return ResponseEntity.noContent().build();
    }
}
