package com.example.bdMetro.controller;

import com.example.bdMetro.entity.AdminPanel;
import com.example.bdMetro.services.AdminPanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-panel")
public class AdminPanelController {

    @Autowired
    private AdminPanelService adminPanelService;

    /** GET /admin-panel — todos los admins */
    @GetMapping
    public List<AdminPanel> getAll() {
        return adminPanelService.getAll();
    }

    /** GET /admin-panel/{id} — por id (ar, uy, co) */
    @GetMapping("/{id}")
    public ResponseEntity<AdminPanel> getById(@PathVariable String id) {
        return adminPanelService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /admin-panel/pais/{pais} — por país */
    @GetMapping("/pais/{pais}")
    public ResponseEntity<AdminPanel> getByPais(@PathVariable String pais) {
        return adminPanelService.getByPais(pais)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /admin-panel/login — verificar credenciales */
    @PostMapping("/login")
    public ResponseEntity<AdminPanel> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return adminPanelService.login(username, password)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    /** PUT /admin-panel/{id} — actualizar nombre, username, password */
    @PutMapping("/{id}")
    public ResponseEntity<AdminPanel> update(@PathVariable String id,
                                             @RequestBody Map<String, String> body) {
        return adminPanelService.update(
                id,
                body.get("nombre"),
                body.get("username"),
                body.get("password")
        ).map(ResponseEntity::ok)
         .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /admin-panel/{id} — solo backend, no expuesto en frontend */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        adminPanelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
