package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.services.ClienteService;
import com.example.bdMetro.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(value = "http://localhost:4200")
public class EmpresaClienteController {

    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private ClienteService clienteService;

    @PostMapping("/upload/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("userCode") String userCode) {
        try {
            String fileName = StringUtils.cleanPath(userCode + "_" + file.getOriginalFilename());
            Path path = Paths.get("uploads/" + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("/uploads/" + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen");
        }
    }

    @GetMapping("/empresas/{userCode}")
    public ResponseEntity<?> getEmpresasByUserCode(@PathVariable String userCode) {
        List<Empresa> empresas = empresaService.getEmpresasByUserCode(userCode);
        if (empresas.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se encontraron empresas para el userCode proporcionado: " + userCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(empresas);
    }

    //http://localhost:8080/api/empresas/id/1
    @GetMapping("/empresas/id/{id}")
    public ResponseEntity<?> getEmpresaById(@PathVariable Long id) {
        Optional<Empresa> empresa = empresaService.getEmpresaById(id);
        if (empresa.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Empresa no encontrada con ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(empresa.get());
    }

    // http://localhost:8080/api/empresas
    @GetMapping("/empresas")
    public ResponseEntity<?> getAllEmpresas() {
        List<Empresa> empresas = empresaService.getAllEmpresas();
        if (empresas.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se encontraron empresas");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
        }
        return ResponseEntity.ok(empresas);
    }


    @PostMapping("/empresas")
    public ResponseEntity<?> pathsEmpresa(@RequestBody Empresa empresa) {
        try {
            Empresa savedEmpresa = empresaService.saveEmpresa(empresa);
            return ResponseEntity.ok(savedEmpresa);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor al guardar la empresa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }




    @PutMapping("/empresas/id/{id}")
    public ResponseEntity<?> updateEmpresa(@PathVariable Long id, @RequestBody Empresa empresaDetails) {
        try {
            Empresa updatedEmpresa = empresaService.updateEmpresa(id, empresaDetails);
            return ResponseEntity.ok(updatedEmpresa);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor al actualizar la empresa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/empresas/id/{id}")
    public ResponseEntity<?> deleteEmpresa(@PathVariable Long id) {
        try {
            empresaService.deleteEmpresa(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor al eliminar la empresa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }





    //http://localhost:8080/api/clientes/{userCode}
    @GetMapping("/clientes/{userCode}")
    public ResponseEntity<List<Cliente>> getClienteByUserCode(@PathVariable String userCode) {
        List<Cliente> clientes = clienteService.getClienteByUserCode(userCode);
        return clientes.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(clientes); // Changed to return List
    }

    //http://localhost:8080/api/clientes/id/{id}
    @GetMapping("/clientes/id/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }





/*
    @PostMapping("/clientes")
    public ResponseEntity<?> saveCliente(@RequestBody Cliente cliente) {
        try {
            Cliente savedCliente = clienteService.saveCliente(cliente);
            return ResponseEntity.ok(savedCliente);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor al guardar el cliente");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }*/


    @PostMapping("/clientes")
    public ResponseEntity<?> saveCliente(@RequestBody Cliente cliente) {
        try {
            Cliente savedCliente = clienteService.saveCliente(cliente);
            return ResponseEntity.ok(savedCliente);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            String errorMessage = "Error interno del servidor al guardar el cliente";
            if (e.getCause() != null && e.getCause().getMessage().contains("NonUniqueResultException")) {
                errorMessage = "Ya existe un cliente registrado con el email '" + cliente.getEmail() + "'";
            } else if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            }
            errorResponse.put("error", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    /*
    @PutMapping("/clientes/id/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        try {
            Cliente updatedCliente = clienteService.updateCliente(id, clienteDetails);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }*/

    @PutMapping("/clientes/id/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        try {
            Cliente updatedCliente = clienteService.updateCliente(id, clienteDetails);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor al actualizar el cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    @DeleteMapping("/clientes/id/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        try {
            clienteService.deleteCliente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/clientes")
    public List<Cliente> getAllClientes() {
        return clienteService.getAllClientes();
    }
}
