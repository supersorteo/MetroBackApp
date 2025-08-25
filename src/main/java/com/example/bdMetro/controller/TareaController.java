package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Tarea;
import com.example.bdMetro.services.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tareas")
//@CrossOrigin(value = "http://localhost:4200")
public class TareaController {

    @Autowired
    private TareaService tareaService;
    //http://localhost:8080/api/tareas
    @GetMapping
    public List<Tarea> getAllTareas() {
        return tareaService.getAllTareas();
    }




    @GetMapping("/by-pais")
    public List<Tarea> getTareasByPais(@RequestParam String pais) {
        return tareaService.getTareasByPais(pais); // Added for country filtering
    }

    @GetMapping("/{id}")
    public Optional<Tarea> getTareaById(@PathVariable Long id) {
        return tareaService.getTareaById(id);
    }


    @PostMapping
    public Tarea addTarea(@RequestBody Tarea tarea) {
        return tareaService.addTarea(tarea);
    }

    @PutMapping("/{id}") public Tarea updateTarea(@PathVariable Long id, @RequestBody Tarea tareaDetails) {
        return tareaService.updateTarea(id, tareaDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteTarea(@PathVariable Long id) {
        tareaService.deleteTarea(id);
    }

}
