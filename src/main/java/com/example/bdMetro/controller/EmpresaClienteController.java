package com.example.bdMetro.controller;

import com.example.bdMetro.entity.Cliente;
import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.entity.Presupuesto;
import com.example.bdMetro.services.ClienteService;
import com.example.bdMetro.services.CloudinaryService;
import com.example.bdMetro.services.EmpresaService;
import com.example.bdMetro.services.PresupuestoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class EmpresaClienteController {


    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private PresupuestoService presupuestoService;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Value("${app.base-url:${APP_BASE_URL:http://localhost:8080}}")
    private String baseUrl;

    private static final String UPLOAD_DIR_LOCAL = "src/main/resources/static/uploads/";
    private static final String UPLOAD_DIR_PROD = "/app/uploads/";
    private static final Logger log = LoggerFactory.getLogger(EmpresaClienteController.class);

    private String getUploadDir() {
        String port = System.getenv("PORT");
        String railwayEnv = System.getenv("RAILWAY_ENVIRONMENT");

        if (port != null || railwayEnv != null) {
            return UPLOAD_DIR_PROD;
        }
        return UPLOAD_DIR_LOCAL;
    }

    private boolean isProduction() {
        String railwayEnv = System.getenv("RAILWAY_ENVIRONMENT");
        return railwayEnv != null && !railwayEnv.isBlank();
    }

    private static final Set<String> ALLOWED_IMAGE_EXTS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private String safeImageExtension(String originalFilename) {
        if (originalFilename == null) throw new IllegalArgumentException("Nombre de archivo nulo");
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0) throw new IllegalArgumentException("El archivo no tiene extension");
        String ext = originalFilename.substring(dot).toLowerCase();
        if (!ALLOWED_IMAGE_EXTS.contains(ext))
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + ext);
        return ext;
    }


    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("userCode") String userCode) {
        List<String> urls = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        try {
            if (isProduction()) {
                // Cloudinary (Railway)
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String publicId = userCode + "_" + System.currentTimeMillis();
                        urls.add(cloudinaryService.upload(file.getBytes(), publicId));
                    }
                }
            } else {
                // Local — disco (UUID + extension allowlist + containment check)
                Path base = Paths.get(getUploadDir()).toAbsolutePath();
                Files.createDirectories(base);
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String ext = safeImageExtension(file.getOriginalFilename());
                        String fileName = UUID.randomUUID() + ext;
                        Path target = base.resolve(fileName).normalize();
                        if (!target.startsWith(base))
                            throw new SecurityException("Path traversal detectado");
                        Files.write(target, file.getBytes());
                        urls.add(baseUrl + "/api/uploads/" + fileName);
                    }
                }
                // --- CODIGO ANTERIOR (vulnerable a path traversal) ---
                // File uploadDir = new File(getUploadDir());
                // if (!uploadDir.exists()) { uploadDir.mkdirs(); }
                // for (MultipartFile file : files) {
                //     if (!file.isEmpty()) {
                //         String fileName = StringUtils.cleanPath(userCode + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
                //         Path filePath = Paths.get(getUploadDir() + fileName);
                //         Files.write(filePath, file.getBytes());
                //         urls.add(baseUrl + "/api/uploads/" + fileName);
                //     }
                // }
            }
            response.put("urls", urls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error al subir las imágenes");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- CODIGO ANTERIOR upload (volumen Railway) ---
    // @PostMapping("/upload/image")
    // public ResponseEntity<Map<String, Object>> uploadImageLocal(
    //         @RequestParam("files") List<MultipartFile> files,
    //         @RequestParam("userCode") String userCode) {
    //     List<String> urls = new ArrayList<>();
    //     Map<String, Object> response = new HashMap<>();
    //     try {
    //         File uploadDir = new File(getUploadDir());
    //         if (!uploadDir.exists()) { uploadDir.mkdirs(); }
    //         for (MultipartFile file : files) {
    //             if (!file.isEmpty()) {
    //                 String fileName = StringUtils.cleanPath(userCode + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
    //                 Path filePath = Paths.get(getUploadDir() + fileName);
    //                 Files.write(filePath, file.getBytes());
    //                 String fileUrl = baseUrl + "/api/uploads/" + fileName;
    //                 urls.add(fileUrl);
    //             }
    //         }
    //         response.put("urls", urls);
    //         return ResponseEntity.ok(response);
    //     } catch (IOException e) {
    //         response.put("error", "Error al subir las imágenes");
    //         response.put("details", e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    //     }
    // }



    // En produccion (Railway) no se usa: Cloudinary sirve las imagenes desde CDN directamente
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path base = Paths.get(getUploadDir()).toAbsolutePath();
            Path filePath = base.resolve(StringUtils.cleanPath(filename)).normalize();
            if (!filePath.startsWith(base)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            if (!filePath.toFile().exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            byte[] imageBytes = Files.readAllBytes(filePath);
            String probed = Files.probeContentType(filePath);
            String contentType = ALLOWED_CONTENT_TYPES.contains(probed) ? probed : "application/octet-stream";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("Content-Security-Policy", "sandbox; default-src 'none'");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline");
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        // --- CODIGO ANTERIOR (sin containment check ni content-type allowlist) ---
        // Path filePath = Paths.get(getUploadDir() + StringUtils.cleanPath(filename));
        // File file = filePath.toFile();
        // if (!file.exists()) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); }
        // byte[] imageBytes = Files.readAllBytes(filePath);
        // String contentType = Files.probeContentType(filePath);
        // if (contentType == null) { contentType = "application/octet-stream"; }
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.parseMediaType(contentType));
        // headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
        // return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    // DISABLED: lista todos los archivos de todos los usuarios sin filtro
    // @GetMapping("/uploads/all")
    // public ResponseEntity<List<String>> getAllImages() {
    //     try {
    //         File folder = new File(getUploadDir());
    //         File[] files = folder.listFiles();
    //         if (files != null) {
    //             List<String> fileUrls = new ArrayList<>();
    //             for (File file : files) {
    //                 if (file.isFile()) {
    //                     String fileUrl = baseUrl + "/api/uploads/" + file.getName();
    //                     fileUrls.add(fileUrl);
    //                 }
    //             }
    //             return ResponseEntity.ok(fileUrls);
    //         } else {
    //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                     .body(Collections.singletonList("No se pudieron obtener las imágenes"));
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body(Collections.singletonList("Error al obtener las imágenes"));
    //     }
    // }

    @DeleteMapping("/uploads/{filename:.+}")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @PathVariable String filename,
            HttpServletRequest request) {
        String userCode = request.getHeader("X-User-Code");
        if (userCode == null || userCode.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticación requerida"));
        }
        try {
            if (isProduction()) {
                String publicId = cloudinaryService.extractPublicId(filename);
                if (!publicId.startsWith("metro/logos/" + userCode + "_")) {
                    log.warn("[Security] Intento de borrar imagen ajena. userCode={} publicId={}", userCode, publicId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Sin permiso para eliminar esta imagen"));
                }
                cloudinaryService.delete(filename);
            } else {
                Path baseDir = Paths.get(getUploadDir()).toAbsolutePath().normalize();
                Path filePath = baseDir.resolve(StringUtils.cleanPath(filename)).normalize();
                if (!filePath.startsWith(baseDir)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Ruta de archivo inválida"));
                }
                if (!Files.exists(filePath)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Imagen no encontrada: " + filename));
                }
                Files.delete(filePath);
            }
            return ResponseEntity.ok(Map.of("message", "Imagen eliminada: " + filename));
        } catch (AccessDeniedException e) {
            log.error("Permisos insuficientes para eliminar: {}", filename, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Permisos insuficientes para eliminar la imagen"));
        } catch (IOException e) {
            log.error("Error IO al eliminar imagen: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar la imagen", "details", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al eliminar imagen: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al eliminar imagen"));
        }
    }

    @GetMapping("/empresas/{userCode}")
    public ResponseEntity<?> getEmpresasByUserCode(@PathVariable String userCode) {
        List<Empresa> empresas = empresaService.getEmpresasByUserCode(userCode);
        return ResponseEntity.ok(empresas);
    }

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

    // DISABLED: expone datos de todos los usuarios sin filtro — usar /empresas/{userCode}
    // @GetMapping("/empresas")
    // public ResponseEntity<?> getAllEmpresas() {
    //     List<Empresa> empresas = empresaService.getAllEmpresas();
    //     if (empresas.isEmpty()) {
    //         Map<String, String> errorResponse = new HashMap<>();
    //         errorResponse.put("error", "No se encontraron empresas");
    //         return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
    //     }
    //     return ResponseEntity.ok(empresas);
    // }


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
            Optional<Empresa> existing = empresaService.getEmpresaById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Empresa no encontrada"));
            }
            if (!existing.get().getUserCode().equals(empresaDetails.getUserCode())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para modificar esta empresa"));
            }
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
    public ResponseEntity<?> deleteEmpresa(@PathVariable Long id, @RequestParam String userCode) {
        try {
            Empresa empresa = empresaService.getEmpresaById(id)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            if (!empresa.getUserCode().equals(userCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para eliminar esta empresa"));
            }

            // Eliminar logo
            String logoUrl = empresa.getLogoUrl();
            if (logoUrl != null && !logoUrl.isBlank()) {
                if (isProduction()) {
                    // Cloudinary — validar que la imagen pertenece al userCode (anti-IDOR)
                    String publicId = cloudinaryService.extractPublicId(logoUrl);
                    if (publicId.startsWith("metro/logos/" + empresa.getUserCode() + "_")) {
                        cloudinaryService.delete(logoUrl);
                    } else {
                        log.warn("[Security] logoUrl no pertenece a userCode={}: {}", empresa.getUserCode(), logoUrl);
                    }
                } else {
                    // Local — disco
                    String fileName = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
                    String safeFileName = StringUtils.cleanPath(fileName);
                    Path filePath = Paths.get(getUploadDir() + safeFileName);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Imagen eliminada al borrar empresa {}: {}", id, safeFileName);
                    }
                }
            }

            empresaService.deleteEmpresa(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            log.error("Permisos insuficientes al eliminar imagen de empresa {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Permisos insuficientes al eliminar imagen"));
        } catch (IOException e) {
            log.error("Error IO al eliminar imagen de empresa {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar la imagen", "details", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar empresa {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }




    @GetMapping("/clientes/{userCode}")
    public ResponseEntity<List<Cliente>> getClienteByUserCode(@PathVariable String userCode) {
        List<Cliente> clientes = clienteService.getClienteByUserCode(userCode);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/clientes/id/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }






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






    @PutMapping("/clientes/id/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        try {
            Optional<Cliente> existing = clienteService.getClienteById(id);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cliente no encontrado"));
            }
            if (!existing.get().getUserCode().equals(clienteDetails.getUserCode())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para modificar este cliente"));
            }
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
    public ResponseEntity<?> deleteCliente(@PathVariable Long id, @RequestParam String userCode) {
        try {
            Optional<Cliente> existing = clienteService.getClienteById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            if (!existing.get().getUserCode().equals(userCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para eliminar este cliente"));
            }
            clienteService.deleteCliente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DISABLED: expone datos de todos los usuarios sin filtro — usar /clientes/{userCode}
    // @GetMapping("/clientes")
    // public List<Cliente> getAllClientes() {
    //     return clienteService.getAllClientes();
    // }

    @GetMapping("/clientes/by-empresa/{empresaId}")
    public ResponseEntity<?> getClientesByEmpresaId(@PathVariable Long empresaId) {
        List<Cliente> clientes = clienteService.getClientesByEmpresaId(empresaId);
        if (clientes.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se encontraron clientes para la empresa con ID: " + empresaId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(clientes);
    }


    @PostMapping("/presupuestos")
    public ResponseEntity<?> savePresupuesto(@RequestBody Presupuesto presupuesto) {
        log.debug("[Presupuesto] POST nombre={} clienteId={} tareas={}",
                presupuesto.getName(),
                presupuesto.getCliente() != null ? presupuesto.getCliente().getId() : "null",
                presupuesto.getTareas() != null ? presupuesto.getTareas().size() : 0);
        Presupuesto saved = presupuestoService.savePresupuesto(presupuesto);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/presupuestos/cliente/{clienteId}")
    public ResponseEntity<List<Presupuesto>> getPresupuestosByClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(presupuestoService.getPresupuestosByClienteId(clienteId));
    }

    @GetMapping("/presupuestos/user/{userCode}")
    public ResponseEntity<Page<Presupuesto>> getAllPresupuestosByUserCode(
            @PathVariable String userCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(presupuestoService.getAllPresupuestosByUserCodePaged(userCode, page, size));
    }

    @GetMapping("/presupuestos/{id}")
    public ResponseEntity<Presupuesto> getPresupuestoById(@PathVariable Long id) {
        return ResponseEntity.ok(presupuestoService.getPresupuestoById(id));
    }

    @DeleteMapping("/presupuestos/{id}")
    public ResponseEntity<?> deletePresupuesto(@PathVariable Long id, @RequestParam String userCode) {
        try {
            Presupuesto existing = presupuestoService.getPresupuestoById(id);
            if (!existing.getUserCode().equals(userCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para eliminar este presupuesto"));
            }
            presupuestoService.deletePresupuesto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/presupuestos/{presupuestoId}/tareas/{tareaId}")
    public ResponseEntity<?> addTareaToPresupuesto(
            @PathVariable Long presupuestoId,
            @PathVariable Long tareaId) {
        try {
            Presupuesto presupuesto = presupuestoService.addTareaToPresupuesto(presupuestoId, tareaId);
            return ResponseEntity.ok(presupuesto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/presupuestos/{presupuestoId}/tareas/{tareaId}")
    public ResponseEntity<?> removeTareaFromPresupuesto(
            @PathVariable Long presupuestoId,
            @PathVariable Long tareaId) {
        try {
            Presupuesto presupuesto = presupuestoService.removeTareaFromPresupuesto(presupuestoId, tareaId);
            return ResponseEntity.ok(presupuesto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DISABLED: expone presupuestos de todos los usuarios sin filtro — usar /presupuestos/user/{userCode}
    // @GetMapping("/presupuestos")
    // public ResponseEntity<?> getAllPresupuestos() {
    //     try {
    //         List<Presupuesto> presupuestos = presupuestoService.getAllPresupuestos();
    //         if (presupuestos.isEmpty()) {
    //             Map<String, String> errorResponse = new HashMap<>();
    //             errorResponse.put("error", "No se encontraron presupuestos");
    //             return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
    //         }
    //         return ResponseEntity.ok(presupuestos);
    //     } catch (Exception e) {
    //         Map<String, String> errorResponse = new HashMap<>();
    //         errorResponse.put("error", "Error interno al obtener todos los presupuestos: " + e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    //     }
    // }


    @PutMapping("/presupuestos/{id}")
    public ResponseEntity<?> updatePresupuesto(
            @PathVariable Long id,
            @RequestBody Presupuesto presupuestoActualizado,
            @RequestHeader(value = "X-User-Code", required = false) String userCodeHeader) {
        try {
            Presupuesto existing = presupuestoService.getPresupuestoById(id);
            String callerCode = (userCodeHeader != null && !userCodeHeader.isBlank())
                    ? userCodeHeader
                    : presupuestoActualizado.getUserCode();
            if (callerCode == null || !existing.getUserCode().equalsIgnoreCase(callerCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sin permiso para modificar este presupuesto"));
            }
            log.debug("[Presupuesto] PUT id={} nombre={} clienteId={} tareas={}",
                    id, presupuestoActualizado.getName(),
                    presupuestoActualizado.getCliente() != null ? presupuestoActualizado.getCliente().getId() : "null",
                    presupuestoActualizado.getTareas() != null ? presupuestoActualizado.getTareas().size() : 0);
            Presupuesto actualizado = presupuestoService.updatePresupuesto(id, presupuestoActualizado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
