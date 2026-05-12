package com.example.bdMetro.controller;

import com.example.bdMetro.entity.TareaPersonalizada;
import com.example.bdMetro.services.TareaPersonalizadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tareas-personalizadas")
public class TareaPersonalizadaController {

    @Autowired
    private TareaPersonalizadaService service;

    @GetMapping("/by-user/{userCode}")
    public List<TareaPersonalizada> getByUser(@PathVariable String userCode) {
        return service.getByUserCode(userCode);
    }

    @PostMapping
    public TareaPersonalizada create(@RequestBody TareaPersonalizada tarea) {
        return service.create(tarea);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaPersonalizada> update(@PathVariable Long id, @RequestBody TareaPersonalizada details) {
        return ResponseEntity.ok(service.update(id, details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
