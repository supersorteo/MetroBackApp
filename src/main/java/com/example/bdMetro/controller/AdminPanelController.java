package com.example.bdMetro.controller;

import com.example.bdMetro.dto.AdminMembershipLimitsDto;
import com.example.bdMetro.entity.AdminPanel;
import com.example.bdMetro.services.AdminPanelService;
import com.example.bdMetro.services.AdminSseService;
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

    @Autowired
    private AdminSseService adminSseService;

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

    @GetMapping("/limits/pais/{pais}")
    public ResponseEntity<AdminMembershipLimitsDto> getLimitsByPais(@PathVariable String pais) {
        return adminPanelService.getLimitsByPais(pais)
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

    @PutMapping("/{id}/limits")
    public ResponseEntity<AdminMembershipLimitsDto> updateLimits(@PathVariable String id,
                                                                 @RequestBody AdminMembershipLimitsDto payload) {
        return adminPanelService.updateLimits(id, payload)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /admin-panel/pais/{pais}/mensaje — mensaje burbuja (público, lo usa el login) */
    @GetMapping("/pais/{pais}/mensaje")
    public ResponseEntity<Map<String, String>> getMensaje(@PathVariable String pais) {
        return adminPanelService.getByPais(pais)
                .map(ap -> ResponseEntity.ok(Map.of("mensaje", ap.getMensajeBurbuja() != null ? ap.getMensajeBurbuja() : "")))
                .orElse(ResponseEntity.ok(Map.of("mensaje", "")));
    }

    /** PUT /admin-panel/{id}/mensaje — actualiza el mensaje desde el panel admin */
    @PutMapping("/{id}/mensaje")
    public ResponseEntity<?> updateMensaje(@PathVariable String id,
                                           @RequestBody Map<String, String> body) {
        String raw = body.get("mensaje");
        // Strip HTML tags and enforce max length to prevent stored XSS
        String sanitized = raw == null ? "" : raw.replaceAll("<[^>]*>", "").trim();
        if (sanitized.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje no puede superar 200 caracteres."));
        }
        try {
            adminPanelService.updateMensaje(id, sanitized);
            adminSseService.broadcastPublic("mensaje-updated", Map.of("mensaje", sanitized));
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /admin-panel/{id} — solo backend, no expuesto en frontend */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        adminPanelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
