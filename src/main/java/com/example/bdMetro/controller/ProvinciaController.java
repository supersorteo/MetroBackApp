package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Provincia;
import com.example.bdMetro.services.ProvinciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/provincias")
//@CrossOrigin(value = "http://localhost:4200")
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
        Optional<Provincia> provincia = provinciaService.getProvinciaById(id);
        if (provincia.isPresent()) {
            return ResponseEntity.ok(provincia.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-pais")
    public ResponseEntity<List<Provincia>> getProvinciasByPais(@RequestParam String pais) {
        List<Provincia> provincias = provinciaService.getProvinciasByPais(pais);
        return ResponseEntity.ok(provincias);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Provincia> updateProvincia(@PathVariable Long id, @RequestBody Provincia provinciaDetails) {
        Provincia updatedProvincia = provinciaService.updateProvincia(id, provinciaDetails);
        return ResponseEntity.ok(updatedProvincia);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvincia(@PathVariable Long id) {
        provinciaService.deleteProvincia(id);
        return ResponseEntity.noContent().build();
    }
}
