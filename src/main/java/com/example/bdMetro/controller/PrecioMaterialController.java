package com.example.bdMetro.controller;

import com.example.bdMetro.entity.PrecioMaterial;
import com.example.bdMetro.services.PrecioMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/precios-materiales")
public class PrecioMaterialController {

    @Autowired private PrecioMaterialService precioMaterialService;

    @GetMapping
    public ResponseEntity<List<PrecioMaterial>> getByPais(@RequestParam String pais) {
        return ResponseEntity.ok(precioMaterialService.getPreciosByPais(pais));
    }
}
