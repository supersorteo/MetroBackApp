package com.example.bdMetro.controller;

import com.example.bdMetro.dto.UserDataSummaryDto;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String deviceId = request.get("deviceId");

        AccessCode accessCode = authenticationService.getCode(code);
        if (accessCode == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("email", "Código no encontrado"));
        } else if (accessCode.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("email", "Código existe pero no asignado a un usuario"));
        } else if (accessCode.isDisabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("email", "Código desactivado. Contactá al administrador."));
        }

        // Crear o refrescar sesión (omitir en modo demo — deviceId nulo o "demo")
        String sessionToken = null;
        if (deviceId != null && !deviceId.equals("demo")) {
            try {
                sessionToken = authenticationService.loginWithDevice(code, deviceId);
            } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error al registrar sesión. Intente nuevamente."));
            }
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("code",             accessCode.getCode());
        response.put("email",            accessCode.getEmail());
        response.put("telefono",         accessCode.getTelefono());
        response.put("pais",             accessCode.getPais());
        response.put("provincia",        accessCode.getProvincia());
        response.put("fechaRegistro",    accessCode.getFechaRegistro() != null ? accessCode.getFechaRegistro().toString() : null);
        response.put("fechaVencimiento", accessCode.getFechaVencimiento() != null ? accessCode.getFechaVencimiento().toString() : null);
        response.put("disabled",         accessCode.isDisabled());
        response.put("selectedEmpresaId", accessCode.getSelectedEmpresaId());
        response.put("selectedClienteId", accessCode.getSelectedClienteId());
        if (sessionToken != null) response.put("sessionToken", sessionToken);
        return ResponseEntity.ok(response);
    }


    // DISABLED: devuelve todos los códigos con emails y datos de todos los usuarios — usar /codes/pais/{pais}
    // @GetMapping("/codes")
    // public List<AccessCode> getAllCodes() {
    //     return authenticationService.getAllCodes();
    // }

    /** GET /auth/codes/pais/{pais} — códigos filtrados por país */
    @GetMapping("/codes/pais/{pais}")
    public List<AccessCode> getCodesByPais(@PathVariable String pais) {
        return authenticationService.getCodesByPais(pais);
    }

    @PostMapping("/agg-codes")
    public ResponseEntity<Map<String, String>> agregarCodes(@RequestBody List<AccessCode> accessCodes) {
        Map<String, String> response = new HashMap<>();
        try {
            authenticationService.agregarCodes(accessCodes);
            response.put("message", "Códigos agregados con éxito");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Error al agregar los códigos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/agg-code")
    public ResponseEntity<Map<String, String>> agregarCode(@RequestBody AccessCode accessCode) {
        Map<String, String> response = new HashMap<>();
        try {
            AccessCode nuevoCode = authenticationService.agregarCode(accessCode);
            response.put("message", "Código agregado con éxito");
            response.put("code", nuevoCode.getCode());
            response.put("email", nuevoCode.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Error al agregar el código: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    //http://localhost:8080/auth/codes
    @PostMapping("/codes")
    public ResponseEntity<Map<String, String>> assignEmail(@RequestBody AccessCode accessCode) {
        Map<String, String> response = new HashMap<>(); try {
            AccessCode updatedCode = authenticationService.addCode(accessCode);
            response.put("message", "Datos asignados con éxito");
            response.put("code", updatedCode.getCode());
            response.put("email", updatedCode.getEmail());
            response.put("telefono", updatedCode.getTelefono());
            response.put("pais", updatedCode.getPais());
            response.put("provincia", updatedCode.getProvincia());
            response.put("fechaRegistro", updatedCode.getFechaRegistro().toString());
            response.put("fechaVencimiento", updatedCode.getFechaVencimiento().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Error al asignar los datos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //http://localhost:8080/auth/codes/{code}
    @GetMapping("/codes/{code}")
    public ResponseEntity<AccessCode> getCode(@PathVariable String code,
                                               @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        boolean sessionActive = true;
        if (sessionToken != null && !sessionToken.isBlank()) {
            sessionActive = authenticationService.updateHeartbeat(sessionToken);
        }
        AccessCode accessCode = authenticationService.getCode(code);
        if (accessCode != null) {
            if (!sessionActive) accessCode.setSessionKicked(true);
            return ResponseEntity.ok(accessCode);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/session/{sessionToken}")
    public ResponseEntity<Void> terminateSession(@PathVariable String sessionToken) {
        authenticationService.terminateSession(sessionToken);
        return ResponseEntity.noContent().build();
    }

    /** Registra retroactivamente una sesión para usuarios logueados antes del deploy de la feature.
     *  Si el límite está lleno, devuelve sessionToken:null en lugar de 409 — el usuario no se kickea. */
    @PostMapping("/session/register")
    public ResponseEntity<?> registerSession(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String deviceId = body.get("deviceId");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "code requerido"));
        }
        try {
            String sessionToken = authenticationService.loginWithDevice(code, deviceId);
            return ResponseEntity.ok(Map.of("sessionToken", sessionToken));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of("sessionToken", ""));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("sessionToken", ""));
        }
    }

    /** Termina una sesión vía POST — permite uso con navigator.sendBeacon() en beforeunload. */
    @PostMapping("/session/terminate")
    public ResponseEntity<Void> terminateSessionBeacon(@RequestBody Map<String, String> body) {
        String sessionToken = body.get("sessionToken");
        if (sessionToken != null && !sessionToken.isBlank()) {
            authenticationService.terminateSession(sessionToken);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/codes/{code}/sessions")
    public ResponseEntity<?> getActiveSessions(@PathVariable String code) {
        AccessCode ac = authenticationService.getCode(code);
        if (ac == null) return ResponseEntity.notFound().build();
        int active = authenticationService.getActiveSessionCount(code);
        int limit = (ac.getMaxDevices() != null) ? ac.getMaxDevices() : 2;
        return ResponseEntity.ok(Map.of("active", active, "limit", limit));
    }

    @GetMapping("/sessions/summary")
    public ResponseEntity<Map<String, Integer>> getSessionsSummary(@RequestParam List<String> codes) {
        Map<String, Integer> result = new HashMap<>();
        for (String code : codes) {
            result.put(code, authenticationService.getActiveSessionCount(code));
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/codes/{code}/sessions")
    public ResponseEntity<Void> terminateAllSessions(@PathVariable String code) {
        authenticationService.terminateAllSessions(code);
        return ResponseEntity.noContent().build();
    }




    @PutMapping("/codes/{code}")
    public ResponseEntity<Map<String, String>> updateCode(@PathVariable String code, @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String telefono = request.get("telefono");
        String provincia = request.get("provincia");
        String pais = request.get("pais"); // Added

        Map<String, String> response = new HashMap<>();
        try {
            AccessCode updatedAccessCode = authenticationService.updateCode(code, email, username, telefono, provincia, pais); // Added pais
            response.put("message", "Código actualizado con éxito");
            response.put("code", updatedAccessCode.getCode());
            response.put("email", updatedAccessCode.getEmail());
            response.put("telefono", updatedAccessCode.getTelefono());
            response.put("provincia", updatedAccessCode.getProvincia());
            response.put("pais", updatedAccessCode.getPais()); // Added
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Error al actualizar el código: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PatchMapping("/codes/{code}/preferences")
    public ResponseEntity<?> updatePreferences(@PathVariable String code, @RequestBody Map<String, Object> body) {
        try {
            Long selectedEmpresaId = body.get("selectedEmpresaId") != null
                    ? ((Number) body.get("selectedEmpresaId")).longValue() : null;
            Long selectedClienteId = body.get("selectedClienteId") != null
                    ? ((Number) body.get("selectedClienteId")).longValue() : null;
            AccessCode ac = authenticationService.updatePreferences(code, selectedEmpresaId, selectedClienteId);
            Map<String, Object> response = new HashMap<>();
            response.put("selectedEmpresaId", ac.getSelectedEmpresaId());
            response.put("selectedClienteId", ac.getSelectedClienteId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reactivate")
    public ResponseEntity<?> reactivateCode(@RequestBody Map<String, String> request) {
        String expiredCode = request.get("expiredCode");
        String newCode = request.get("newCode");
        String email = request.get("email");
        String deviceId = request.get("deviceId");
        try {
            AccessCode updated = authenticationService.reactivateCode(expiredCode, newCode, email, deviceId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("email", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("email", "Error al reactivar: " + e.getMessage()));
        }
    }

    @DeleteMapping("/codes/{code}")
    public void deleteCode(@PathVariable String code) {
        authenticationService.deleteCode(code);
    }

    @GetMapping("/codes/{code}/summary")
    public ResponseEntity<?> getUserDataSummary(@PathVariable String code) {
        UserDataSummaryDto summary = authenticationService.getUserDataSummary(code);
        if (summary == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(summary);
    }

    @DeleteMapping("/codes/{code}/full")
    public ResponseEntity<?> deleteUserData(@PathVariable String code) {
        try {
            authenticationService.deleteUserData(code);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Edición de fechas desde el panel de administración.
    @PatchMapping("/codes/{code}/expiry")
    public ResponseEntity<?> setExpiry(@PathVariable String code, @RequestBody Map<String, String> body) {
        String vencStr = body.get("fechaVencimiento");
        String regStr  = body.get("fechaRegistro");
        if (vencStr == null || vencStr.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "fechaVencimiento requerida."));
        try {
            LocalDate venc = LocalDate.parse(vencStr);
            AccessCode ac = authenticationService.getCode(code);
            if (ac == null) return ResponseEntity.notFound().build();
            ac.setFechaVencimiento(venc);
            if (regStr != null && !regStr.isBlank()) ac.setFechaRegistro(LocalDate.parse(regStr));
            authenticationService.save(ac);
            return ResponseEntity.ok(Map.of(
                "code", ac.getCode(),
                "fechaVencimiento", vencStr,
                "fechaRegistro", regStr != null ? regStr : (ac.getFechaRegistro() != null ? ac.getFechaRegistro().toString() : "")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/codes/{code}/max-devices")
    public ResponseEntity<?> updateMaxDevices(@PathVariable String code, @RequestBody Map<String, Object> body) {
        try {
            AccessCode ac = authenticationService.getCode(code);
            if (ac == null) return ResponseEntity.notFound().build();
            Integer maxDevices = null;
            if (body.get("maxDevices") != null) {
                maxDevices = Integer.parseInt(body.get("maxDevices").toString());
            }
            ac.setMaxDevices(maxDevices);
            authenticationService.save(ac);
            return ResponseEntity.ok(Map.of("code", ac.getCode(), "maxDevices", maxDevices != null ? maxDevices : 2));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/codes/{code}/disable")
    public ResponseEntity<?> disableCode(@PathVariable String code) {
        try {
            AccessCode ac = authenticationService.disableCode(code);
            return ResponseEntity.ok(Map.of("code", ac.getCode(), "disabled", ac.isDisabled()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/codes/{code}/enable")
    public ResponseEntity<?> enableCode(@PathVariable String code) {
        try {
            AccessCode ac = authenticationService.enableCode(code);
            return ResponseEntity.ok(Map.of("code", ac.getCode(), "disabled", ac.isDisabled()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user-code/{code}")
    public ResponseEntity<AccessCode> getUserCode(@PathVariable String code) {
        AccessCode accessCode = authenticationService.getCode(code);
        if (accessCode == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(accessCode);
    }

    @PutMapping("/last-presupuesto")
    public ResponseEntity<?> saveLastPresupuesto(jakarta.servlet.http.HttpServletRequest req,
                                                  @RequestBody Map<String, Long> body) {
        String userCode = req.getHeader("X-User-Code");
        if (userCode == null || userCode.isBlank()) return ResponseEntity.status(401).build();
        authenticationService.saveLastPresupuesto(userCode, body.get("id"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/last-presupuesto/{userCode}")
    public ResponseEntity<Map<String, Long>> getLastPresupuesto(@PathVariable String userCode) {
        Long id = authenticationService.getLastPresupuesto(userCode);
        return id != null ? ResponseEntity.ok(Map.of("id", id)) : ResponseEntity.noContent().build();
    }

}
