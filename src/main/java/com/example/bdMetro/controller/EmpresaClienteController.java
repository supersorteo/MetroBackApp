package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.services.ClienteService;
import com.example.bdMetro.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
//@CrossOrigin(value = "http://localhost:4200")
public class EmpresaClienteController {


    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private ClienteService clienteService;

    @Value("${app.base-url:http://localhost:8080}")
    //@Value("${app.base-url:${APP_BASE_URL}}")
    private String baseUrl;

    private static final String UPLOAD_DIR_LOCAL = "src/main/resources/static/uploads/";
    //private static final String UPLOAD_DIR_PROD = "/app/uploads/";

    private static final String UPLOAD_DIR_PROD = "/data/uploads/";


   /* private String getUploadDir() {
        String env = System.getenv("RAILWAY_ENVIRONMENT");
        return env != null && !env.isEmpty() ? UPLOAD_DIR_PROD : UPLOAD_DIR_LOCAL;
    }*/

    private String getUploadDir() {
        String port = System.getenv("PORT");
        String railwayEnv = System.getenv("RAILWAY_ENVIRONMENT");

        if (port != null || railwayEnv != null) {
            return UPLOAD_DIR_PROD;
        }
        return UPLOAD_DIR_LOCAL;
    }


    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("userCode") String userCode) {
        List<String> urls = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        try {
            File uploadDir = new File(getUploadDir());
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = StringUtils.cleanPath(userCode + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
                    Path filePath = Paths.get(getUploadDir() + fileName);
                    Files.write(filePath, file.getBytes());
                    String fileUrl = baseUrl + "/api/uploads/" + fileName;
                    urls.add(fileUrl);
                }
            }
            response.put("urls", urls);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "Error al subir las imágenes");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(getUploadDir() + StringUtils.cleanPath(filename));
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/uploads/all")
    public ResponseEntity<List<String>> getAllImages() {
        try {
            File folder = new File(getUploadDir());
            File[] files = folder.listFiles();
            if (files != null) {
                List<String> fileUrls = new ArrayList<>();
                for (File file : files) {
                    if (file.isFile()) {
                        String fileUrl = baseUrl + "/api/uploads/" + file.getName();
                        fileUrls.add(fileUrl);
                    }
                }
                return ResponseEntity.ok(fileUrls);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonList("No se pudieron obtener las imágenes"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Error al obtener las imágenes"));
        }
    }

    @DeleteMapping("/uploads/{filename:.+}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(getUploadDir() + StringUtils.cleanPath(filename));
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Imagen no encontrada"));
            }
            boolean deleted = file.delete();
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Imagen eliminada con éxito"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "No se pudo eliminar la imagen"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar la imagen", "details", e.getMessage()));
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
    public ResponseEntity<?> postEmpresa(@RequestBody Empresa empresa) {
        try {
            if (empresa.getName() == null || empresa.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El nombre de la empresa es obligatorio"));
            }
            if (empresa.getUserCode() == null || empresa.getUserCode().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El userCode es obligatorio"));
            }
            Empresa savedEmpresa = empresaService.saveEmpresa(empresa);
            return ResponseEntity.ok(savedEmpresa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al guardar la empresa", "details", e.getMessage()));
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
