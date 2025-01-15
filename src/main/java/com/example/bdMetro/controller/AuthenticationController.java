package com.example.bdMetro.controller;

import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.example.bdMetro.entity.AccessCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(value = "http://localhost:4200")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    private AccessCodeRepository accessCodeRepository;

    @CrossOrigin(value = "http://localhost:4200")

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String result = authenticationService.login(code);
        Map<String, String> response = new HashMap<>();
        response.put("email", result);
        if (!result.equals("Código no encontrado")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


   //http://localhost:8080/auth/codes
    @GetMapping("/codes")
    public List<AccessCode> getAllCodes() {
        return authenticationService.getAllCodes();
    }

    @PostMapping("/agg-codes")
    public ResponseEntity<Map<String, String>> agregarCodes(@RequestBody List<AccessCode> accessCodes) {
        Map<String, String> response = new HashMap<>();
        try {
            List<AccessCode> nuevosCodes = authenticationService.agregarCodes(accessCodes);
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
            response.put("username", updatedCode.getUsername());
            response.put("telefono", updatedCode.getTelefono());
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
    public ResponseEntity<AccessCode> getCode(@PathVariable String code) {
        AccessCode accessCode = authenticationService.getCode(code);
        if (accessCode != null) {
            return ResponseEntity.ok(accessCode);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("/codes/{code}")
    public ResponseEntity<Map<String, String>> updateCode(@PathVariable String code, @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String telefono = request.get("telefono");
        String provincia = request.get("provincia");

        Map<String, String> response = new HashMap<>();
        try {
            AccessCode updatedAccessCode = authenticationService.updateCode(code, email, username, telefono, provincia);
            response.put("message", "Código actualizado con éxito");
            response.put("code", updatedAccessCode.getCode());
            response.put("email", updatedAccessCode.getEmail());
            response.put("username", updatedAccessCode.getUsername());
            response.put("telefono", updatedAccessCode.getTelefono());
            response.put("provincia", updatedAccessCode.getProvincia());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Error al actualizar el código: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @CrossOrigin(value = "http://localhost:4200")
    @DeleteMapping("/codes/{code}")
    public void deleteCode(@PathVariable String code) {
        authenticationService.deleteCode(code);
    }

    @GetMapping("/user-code/{code}")
    public ResponseEntity<AccessCode> getUserCode(@PathVariable String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(accessCode);
    }

}
