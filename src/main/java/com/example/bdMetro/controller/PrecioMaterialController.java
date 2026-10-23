package com.example.bdMetro.controller;

import com.example.bdMetro.entity.PrecioMaterial;
import com.example.bdMetro.services.PrecioMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/precios-materiales")
public class PrecioMaterialController {

    @Autowired private PrecioMaterialService precioMaterialService;

    @GetMapping
    public ResponseEntity<List<PrecioMaterial>> getByPais(@RequestParam String pais) {
        return ResponseEntity.ok(precioMaterialService.getPreciosByPais(pais));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────

    @GetMapping("/admin")
    public ResponseEntity<Page<PrecioMaterial>> getAdminPage(
            @RequestParam String pais,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(required = false) List<String> categorias) {
        return ResponseEntity.ok(precioMaterialService.getAdminPage(pais, page, size, buscar,
                categorias != null ? categorias : Collections.emptyList()));
    }

    @GetMapping("/admin/categorias")
    public ResponseEntity<List<String>> getCategorias(@RequestParam String pais) {
        return ResponseEntity.ok(precioMaterialService.getCategoriasByPais(pais));
    }

    @PostMapping("/admin")
    public ResponseEntity<PrecioMaterial> create(@RequestBody PrecioMaterial pm) {
        return ResponseEntity.status(201).body(precioMaterialService.create(pm));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<PrecioMaterial> update(@PathVariable Long id, @RequestBody PrecioMaterial pm) {
        return ResponseEntity.ok(precioMaterialService.update(id, pm));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        precioMaterialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
