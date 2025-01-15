package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Provincia;
import com.example.bdMetro.services.ProvinciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provincias")
@CrossOrigin(value = "http://localhost:4200")
public class ProvinciaController {
    @Autowired
    private ProvinciaService provinciaService;
    //http://localhost:8080/api/provincias
    @PostMapping
    public ResponseEntity<Provincia> addProvincia(@RequestBody Provincia provincia) {
        Provincia nuevaProvincia = provinciaService.addProvincia(provincia);
        return ResponseEntity.ok(nuevaProvincia);
    }

    @GetMapping
    public ResponseEntity<List<Provincia>> getAllProvincias() {
        List<Provincia> provincias = provinciaService.getAllProvincias();
        return ResponseEntity.ok(provincias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provincia> getProvinciaById(@PathVariable Long id) {
        Provincia provincia = provinciaService.getProvinciaById(id);
        if (provincia != null) {
            return ResponseEntity.ok(provincia);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
