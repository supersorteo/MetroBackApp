package com.example.bdMetro.services;

import com.example.bdMetro.entity.Empresa;
import com.example.bdMetro.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {
    @Autowired
    private EmpresaRepository empresaRepository;

    private static final String UPLOAD_DIR_LOCAL = "src/main/resources/static/uploads/";
    private static final String UPLOAD_DIR_PROD = "/app/uploads/";

    private String getUploadDir() {
        String env = System.getenv("RAILWAY_ENVIRONMENT");
        return env != null && !env.isEmpty() ? UPLOAD_DIR_PROD : UPLOAD_DIR_LOCAL;
    }

    public List<Empresa> getEmpresasByUserCode(String userCode) {  // Plural y List
        return empresaRepository.findByUserCode(userCode);
    }


    public Optional<Empresa> getEmpresaById(Long id) {
        return empresaRepository.findById(id);
    }

    public List<Empresa> getAllEmpresas() {
        return empresaRepository.findAll();
    }

    /*
    public Empresa saveEmpresa(Empresa empresa) {
        return empresaRepository.save(empresa);
    }*/


    /*
    public Empresa saveEmpresa(Empresa empresa) {
        if (empresa.getUserCode() == null || empresa.getUserCode().isEmpty()) {
            throw new IllegalArgumentException("El userCode es requerido para guardar la empresa");
        }
        Empresa existingEmpresa = empresaRepository.findByUserCodeAndIdNot(empresa.getUserCode(), -1L);
        if (existingEmpresa != null) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el userCode '" + empresa.getUserCode() + "'");
        }
        return empresaRepository.save(empresa);
    }*/



    /*
    public Empresa saveEmpresa(Empresa empresa) {
        if (empresa.getUserCode() == null || empresa.getUserCode().isEmpty()) {
            throw new IllegalArgumentException("El userCode es requerido para guardar la empresa");
        }
        return empresaRepository.save(empresa);
    }*/

    public Empresa saveEmpresa(Empresa empresa) {
        if (empresa.getName() == null || empresa.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        if (empresa.getUserCode() == null || empresa.getUserCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El userCode es obligatorio");
        }
        if (empresa.getLogoUrl() != null && !empresa.getLogoUrl().isEmpty()) {
            Path filePath = Paths.get(getUploadDir() + empresa.getLogoUrl().substring(empresa.getLogoUrl().lastIndexOf("/") + 1));
            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("La imagen especificada no existe en el servidor");
            }
        }
        return empresaRepository.save(empresa);
    }

    public Empresa updateEmpresa(Long id, Empresa empresaDetails) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        empresa.setName(empresaDetails.getName());
        empresa.setPhone(empresaDetails.getPhone());
        empresa.setEmail(empresaDetails.getEmail());
        empresa.setDescription(empresaDetails.getDescription());
        empresa.setLogoUrl(empresaDetails.getLogoUrl());
        return empresaRepository.save(empresa);
    }

    /*
    public void deleteEmpresa(Long id) {
        empresaRepository.deleteById(id);
    }*/



    /*
    public void deleteEmpresa(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new RuntimeException("Empresa no encontrada");
        }
        empresaRepository.deleteById(id);
    }*/


    public void deleteEmpresa(Long id) {
        Optional<Empresa> optionalEmpresa = empresaRepository.findById(id);
        if (optionalEmpresa.isEmpty()) {
            throw new RuntimeException("Empresa no encontrada");
        }
        Empresa empresa = optionalEmpresa.get();
        if (empresa.getLogoUrl() != null && !empresa.getLogoUrl().isEmpty()) {
            String filename = empresa.getLogoUrl().substring(empresa.getLogoUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(getUploadDir() + filename);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar la imagen asociada: " + e.getMessage());
            }
        }
        empresaRepository.deleteById(id);
    }

}
